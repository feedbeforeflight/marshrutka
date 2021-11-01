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

    public HandledMessage receive(BrokerPoint receiver) throws TransferException {
        return messageBroker.receive(receiver);
    }

    @Override
    public boolean messageReceived(HandledMessage message) {
        return false;
    }
}
