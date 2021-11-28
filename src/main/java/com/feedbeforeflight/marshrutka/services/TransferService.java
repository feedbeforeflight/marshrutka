package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class TransferService implements MessageBrokerServiceNotificationClient {

    private final MessageBroker messageBroker;
    private final MessageBrokerManager messageBrokerManager;

    public TransferService(MessageBroker messageBroker, MessageBrokerManager messageBrokerManager) {
        this.messageBroker = messageBroker;
        this.messageBrokerManager = messageBrokerManager;
    }

    @PostConstruct
    public void Initialize() {
        messageBrokerManager.applyConfiguration();
        messageBroker.registerNotificationClient(this);
    }

    public void sendDirect(HandledMessage message) throws TransferException {
        log.debug("Sending message");

        try {
            messageBroker.send(message);
        }
        catch (AmqpException amqpException) {
            throw new TransferException(amqpException.getMessage(), amqpException);
        }
    }

    public void sendDirect(BrokerPoint sourcePoint,
                           BrokerPoint destinationPoint,
                           String flowName, String message) throws TransferException{
        HandledMessage handledMessage = messageBroker.createHandledMessage(sourcePoint.getName(), destinationPoint.getName(), flowName, message);

        sendDirect(handledMessage);
    }

    public HandledMessage receive(BrokerPoint receiver) throws TransferException {
        return messageBroker.receive(receiver);
    }

    public BrokerPoint getPoint(String name) {
        return messageBroker.getPoint(name).orElse(null);
    }

    @Override
    public boolean messageReceived(HandledMessage message) {
        return false;
    }
}
