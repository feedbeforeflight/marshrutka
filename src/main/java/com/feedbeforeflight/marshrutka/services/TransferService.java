package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.Message;
import com.feedbeforeflight.marshrutka.models.Point;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class TransferService {

    private final PointRepository pointRepository;

    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    public TransferService(PointRepository pointRepository, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.pointRepository = pointRepository;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @PostConstruct
    public void Initialize() {

    }

    public Point getPoint(int id) {
        return pointRepository.findById(id).orElse(null);
    }

    public void sendDirect(Message message) throws TransferException {
        try {
            amqpTemplate.convertAndSend(message.getDestination().getName(), message);
        }
        catch (AmqpException amqpException) {
            throw new TransferException(amqpException.getMessage(), amqpException);
        }
    }

    public Message receive(Point receiver) throws TransferException{
        return amqpTemplate.receiveAndConvert(
                receiver.getName(),
                new ParameterizedTypeReference<Message>() {});
    }

}
