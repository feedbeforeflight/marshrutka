package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.services.TransferException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class RabbitMessageBroker implements MessageBroker, MessageBrokerManager {

    private final PointRepository pointRepository;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;
    private final ApplicationContext applicationContext;

    private final HashMap<String, BrokerPoint> pointMap;

    private MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient;

    public RabbitMessageBroker(ApplicationContext applicationContext, PointRepository pointRepository, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.pointRepository = pointRepository;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;
        this.applicationContext = applicationContext;

        this.pointMap = new HashMap<>();
    }

    // Broker repository
    @Override
    public Optional<BrokerPoint> getPoint(String name) {
        return Optional.ofNullable(pointMap.get(name));
    }

    @Override
    public Message wrapMessage(String sourcePointName, String destinationPointName, String payload) throws TransferException {
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

        return new Message(sourcePoint, destinationPoint, payload);
    }

    //Broker itself
    @Override
    public void send(Message message) throws TransferException {
        if (message.getDestination() != null) {
            amqpTemplate.convertAndSend(message.getDestination().getName(), message.getPayload());
        }
        else {
            amqpTemplate.convertAndSend(message.getSource().getName(), "", message.getPayload());
        }
    }

    @Override
    public void send(BrokerPoint source, BrokerPoint destination, String message) throws TransferException {
        send(new Message(source, destination, message));
    }

    @Override
    public void send(String sourcePointName, String destinationPointName, String message) throws TransferException {
        send(wrapMessage(sourcePointName, destinationPointName, message));
    }

    @Override
    public Message receive(BrokerPoint destination) throws TransferException {
        Object received = amqpTemplate.receiveAndConvert(destination.getName());

        return received == null ? null : new Message(null, destination, received.toString());
    }

    @Override
    public Message receive(String destinationPointName) throws TransferException {
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
}
