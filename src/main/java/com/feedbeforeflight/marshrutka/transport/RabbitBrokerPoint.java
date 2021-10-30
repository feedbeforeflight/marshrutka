package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.rabbitmq.RabbitMessageReceiver;
import lombok.Getter;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.ApplicationContext;

public class RabbitBrokerPoint implements BrokerPoint {

    private int id;
    private String name;
    @Getter private String receiveURL;

    private final ApplicationContext applicationContext;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    @Getter private SimpleMessageListenerContainer container;

    private Queue queue;

    public RabbitBrokerPoint(ApplicationContext applicationContext, AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.applicationContext = applicationContext;
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init(PointEntity pointEntity) {
        this.id = pointEntity.getId();
        this.name = pointEntity.getName();
        this.receiveURL = pointEntity.getReceiveURL();

        // todo: if it should receive smth, set listener container
        this.queue = new Queue(this.name);
        amqpAdmin.declareQueue(this.queue);

        this.container = applicationContext.getBean(SimpleMessageListenerContainer.class);
        container.setQueueNames(this.queue.getName());
        container.setMessageListener(new MessageListenerAdapter(new RabbitMessageReceiver(this), "receiveMessage"));
        container.initialize();
        container.start();
    }

    @Override
    public void powerOff() {
        // todo: power off the container
        //amqpAdmin.deleteQueue(this.queue.getName()); // maybe dropping potentially not empty queue is not a good idea
        container.shutdown();
        container.removeQueueNames(this.queue.getName());
    }

}
