package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.transport.*;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
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

    public void sendDirect(Message message) throws TransferException {
        try {
            messageBroker.send(message);
        }
        catch (AmqpException amqpException) {
            throw new TransferException(amqpException.getMessage(), amqpException);
        }
    }

    public Message receive(BrokerPoint receiver) throws TransferException {
        return messageBroker.receive(receiver);
    }

    @Override
    public boolean messageReceived(Message message) {
        return false;
    }
}
