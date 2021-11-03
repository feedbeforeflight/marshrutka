package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.PointLiveData;
import com.feedbeforeflight.marshrutka.services.TransferException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;
import java.util.stream.StreamSupport;

public class RabbitMessageBroker implements MessageBroker, MessageBrokerManager, ApplicationContextAware {

    private final PointRepository pointRepository;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;
    private ApplicationContext applicationContext;
    private final MessageConverter messageConverter;

    private final HashMap<String, BrokerPoint> pointMap;

    private MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient;

    public RabbitMessageBroker(PointRepository pointRepository, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.pointRepository = pointRepository;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;

        this.pointMap = new HashMap<>();
        this.messageConverter = new SimpleMessageConverter();
    }

    // Broker repository
    @Override
    public Optional<BrokerPoint> getPoint(String name) {
        return Optional.ofNullable(pointMap.get(name));
    }

    @Override
    public Optional<BrokerPoint> getPointByID(int id) {
        for (BrokerPoint brokerPoint : pointMap.values()) {
            if (id == brokerPoint.getId()) {
                return Optional.of(brokerPoint);
            }
        }

        return Optional.empty();
    }

    @Override
    public HandledMessage createHandledMessage(String sourcePointName, String destinationPointName, String flowName, String payload) throws TransferException {
        BrokerPoint sourcePoint = null;
        BrokerPoint destinationPoint = null;

        if (!sourcePointName.isEmpty()) {
            sourcePoint = getPoint(sourcePointName)
                    .orElseThrow(() -> new TransferException(String.format("Source point [%s] not found", sourcePointName)));
        }
        if (!destinationPointName.isEmpty()) {
            destinationPoint = getPoint(destinationPointName)
                    .orElseThrow(() -> new TransferException(String.format("Destination point [%s] not found", destinationPointName)));
        }

        return new HandledMessage(sourcePoint, destinationPoint, flowName, payload);
    }

    //Broker itself
    @Override
    public void send(HandledMessage handledMessage) throws TransferException {
        // todo: should transfer not only flowName, but set of headers, such as sourcePointID etc. to discover for handledMessage on the other side
        if (handledMessage.getDestination() != null) {
            amqpTemplate.convertAndSend(handledMessage.getDestination().getName(), handledMessage.getPayload(), m -> {
                m.getMessageProperties().setHeader("flowName", handledMessage.getFlowName());
                return m;
            });
        }
        else {
            amqpTemplate.convertAndSend(handledMessage.getSource().getName(), "", handledMessage.getPayload(), m -> {
                m.getMessageProperties().setHeader("flowName", handledMessage.getFlowName());
                return m;
            });
        }
        handledMessage.getSource().increaseSentCount();
    }

    @Override
    public void send(BrokerPoint source, BrokerPoint destination, String flowName, String message) throws TransferException {
        send(new HandledMessage(source, destination, flowName, message));
    }

    @Override
    public void send(String sourcePointName, String destinationPointName, String flowName, String message) throws TransferException {
        send(createHandledMessage(sourcePointName, destinationPointName, flowName, message));
    }

    @Override
    public HandledMessage receive(BrokerPoint destination) throws TransferException {
        Object received = amqpTemplate.receiveAndConvert(destination.getName());

        return received == null ? null : new HandledMessage(null, destination, "", received.toString());
    }

    @Override
    public HandledMessage receive(String destinationPointName) throws TransferException {
        BrokerPoint destinationPoint = null;

        if (!destinationPointName.isEmpty()) {
            destinationPoint = getPoint(destinationPointName)
                    .orElseThrow(() -> new TransferException(String.format("Destination point [%s] not found", destinationPointName)));
        }

        return receive(destinationPoint);
    }

    @Override
    public void registerNotificationClient(MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient) {
        this.messageBrokerServiceNotificationClient = messageBrokerServiceNotificationClient;
    }

    @Override
    public void applyConfiguration() {
        pointMap.forEach((name, brokerPoint) -> brokerPoint.powerOffListener());
        pointMap.clear();

        StreamSupport.stream(pointRepository.findAll().spliterator(), false)
                .forEach(point -> {
                    BrokerPoint brokerPoint = applicationContext.getBean(BrokerPoint.class);
                    brokerPoint.init(point);

                    pointMap.put(point.getName(), brokerPoint);
                });

    }

    @Override
    public List<BrokerPoint> getPointList() {
        return new ArrayList<>(pointMap.values());
    }

    @Override
    public PointLiveData getPointLiveData(int pointID) {
        BrokerPoint brokerPoint = getPointByID(pointID).orElse(null);
        if (brokerPoint == null) {
            return new PointLiveData();
        }
        else {
            return new PointLiveData(
                    pointID,
                    brokerPoint.getMessagesSent(),
                    brokerPoint.getMessagesReceived(),
                    brokerPoint.getMessagesQueued(),
                    brokerPoint.receiveSuspended());
        }
    }

    @Override
    public List<PointLiveData> getLiveData() {
        List<PointLiveData> pointLiveDataList = new ArrayList<>();

        pointMap.values().stream()
                .forEach(brokerPoint -> pointLiveDataList.add(new PointLiveData(
                        brokerPoint.getId(),
                        brokerPoint.getMessagesSent(),
                        brokerPoint.getMessagesReceived(),
                        brokerPoint.getMessagesQueued(),
                        brokerPoint.receiveSuspended())));
        return pointLiveDataList;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
