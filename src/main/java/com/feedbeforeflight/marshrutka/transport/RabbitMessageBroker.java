package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.services.TransferException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class RabbitMessageBroker implements MessageBroker, MessageBrokerManager, ApplicationContextAware {

    private final PointRepository pointRepository;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;
    private ApplicationContext applicationContext;
    private MessageConverter messageConverter;

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
    public HandledMessage createHandledMessage(String sourcePointName, String destinationPointName, String brookName, String payload) throws TransferException {
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

        return new HandledMessage(sourcePoint, destinationPoint, brookName, payload);
    }

    private Message convertHandledMessageToAMQPMessage(HandledMessage handledMessage) {



        return null;
    }

    //Broker itself
    @Override
    public void send(HandledMessage handledMessage) throws TransferException {
        // todo: should transfer not only brookName, but set of headers, such as sourcePointID etc. to discover for handledMessage on the other side
        if (handledMessage.getDestination() != null) {
            amqpTemplate.convertAndSend(handledMessage.getDestination().getName(), handledMessage.getPayload(), m -> {
                m.getMessageProperties().setHeader("brookName", handledMessage.getBrookName());
                return m;
            });
        }
        else {
            amqpTemplate.convertAndSend(handledMessage.getSource().getName(), "", handledMessage.getPayload(), m -> {
                m.getMessageProperties().setHeader("brookName", handledMessage.getBrookName());
                return m;
            });
        }
    }

    @Override
    public void send(BrokerPoint source, BrokerPoint destination, String brookName, String message) throws TransferException {
        send(new HandledMessage(source, destination, brookName, message));
    }

    @Override
    public void send(String sourcePointName, String destinationPointName, String brookName, String message) throws TransferException {
        send(createHandledMessage(sourcePointName, destinationPointName, brookName, message));
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
        pointMap.forEach((name, brokerPoint) -> brokerPoint.powerOff());
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
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
