package com.feedbeforeflight.marshrutka.rabbitmq;

import com.feedbeforeflight.marshrutka.transport.RabbitBrokerPoint;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.SimpleMessageConverter;

import java.util.List;

public class RabbitMessageReceiver implements MessageListener {

    private final RabbitBrokerPoint rabbitBrokerPoint;
    private AcknowledgeMode acknowledgeMode;

    private final MessageConverter messageConverter;

    public RabbitMessageReceiver(RabbitBrokerPoint rabbitBrokerPoint) {
        this.rabbitBrokerPoint = rabbitBrokerPoint;

        this.messageConverter = new SimpleMessageConverter();
    }

    @Override
    public void onMessage(Message message) {
        rabbitBrokerPoint.messageReceived(
                (String) this.messageConverter.fromMessage(message),
                message.getMessageProperties().getHeader("flowName"));
    }

    @Override
    public void containerAckMode(AcknowledgeMode mode) {
        this.acknowledgeMode = mode;
    }

    @Override
    public void onMessageBatch(List<Message> messages) {
        MessageListener.super.onMessageBatch(messages);
    }
}
