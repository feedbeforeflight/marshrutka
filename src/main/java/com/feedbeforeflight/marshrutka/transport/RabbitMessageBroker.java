package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.services.TransferException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;

import java.util.HashMap;
import java.util.Optional;
import java.util.stream.StreamSupport;

public class RabbitMessageBroker implements MessageBroker, MessageBrokerManager {

    private final PointRepository pointRepository;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    private final HashMap<String, BrokerPoint> pointMap;

    private MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient;

    public RabbitMessageBroker(PointRepository pointRepository, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.pointRepository = pointRepository;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;

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
    public boolean send(Message message) {
        return false;
    }

    @Override
    public boolean send(BrokerPoint source, BrokerPoint destination, String message) {
        return true;
    }

    @Override
    public boolean send(String sourcePointName, String destinationPointName, String Message) {

        return true;
    }

    @Override
    public Message receive(BrokerPoint destination) {
        return null;
    }

    @Override
    public Message receive(String destinationPointName) {
        return null;
    }

    @Override
    public void registerNotificationClient(MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient) {
        this.messageBrokerServiceNotificationClient = messageBrokerServiceNotificationClient;
    }

    @Override
    public void ApplyConfiguration() {
        pointMap.clear();

        StreamSupport.stream(pointRepository.findAll().spliterator(), false)
                .forEach(point -> pointMap.put(point.getName(), new RabbitBrokerPoint(point.getId(), point.getName())));
    }
}
