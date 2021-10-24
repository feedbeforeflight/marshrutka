package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.Message;
import com.feedbeforeflight.marshrutka.transport.MessageBroker;
import com.feedbeforeflight.marshrutka.transport.MessageBrokerServiceNotificationClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransferService implements MessageBrokerServiceNotificationClient {

    private final MessageBroker messageBroker;

    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    public TransferService(MessageBroker messageBroker, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.messageBroker = messageBroker;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @PostConstruct
    public void Initialize() {
        messageBroker.registerNotificationClient(this);
    }

    public void sendDirect(Message message) throws TransferException {
        try {
            amqpTemplate.convertAndSend(message.getDestination().getName(), message);
        }
        catch (AmqpException amqpException) {
            throw new TransferException(amqpException.getMessage(), amqpException);
        }
    }

    public Message receive(BrokerPoint receiver) throws TransferException{
        return amqpTemplate.receiveAndConvert(
                receiver.getName(),
                new ParameterizedTypeReference<Message>() {});
    }

    @Override
    public boolean messageReceived(Message message) {
        return false;
    }
}
