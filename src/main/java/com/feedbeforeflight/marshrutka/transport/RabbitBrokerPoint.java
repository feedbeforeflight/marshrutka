package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.rabbitmq.RabbitMessageReceiver;
import com.feedbeforeflight.marshrutka.services.TransferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
public class RabbitBrokerPoint implements BrokerPoint, ApplicationContextAware {

    @Getter private int id;
    @Getter private String name;
    @Getter private String receiveURL;

    private ApplicationContext applicationContext;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    @Getter private SimpleMessageListenerContainer container;

    private Queue queue;

    @Getter private long lastSendToClientAttempt = 0;
    @Getter boolean sendToClientFault = false;

    public RabbitBrokerPoint(AmqpTemplate amqpTemplate, AmqpAdmin amqpAdmin) {
        this.amqpTemplate = amqpTemplate;
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public boolean receiveSuspended() {
        return sendToClientFault;
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
        container.setMessageListener(new MessageListenerAdapter(new RabbitMessageReceiver(this)));
        container.initialize();
        container.start();
    }

    @Override
    public void powerOff() {
        //amqpAdmin.deleteQueue(this.queue.getName()); // maybe dropping potentially not empty queue is not a good idea
        container.shutdown();
        container.removeQueueNames(this.queue.getName());
    }

    @Override
    public void messageReceived(String message, String brookName) throws TransferException {
        log.debug("Receiving message. Brook name: " + brookName);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        headers.set("X-brook-name", brookName);

        HttpEntity<String> request = new HttpEntity<>(message, headers);

        try {
            String result = restTemplate.postForObject(receiveURL, request, String.class);
            lastSendToClientAttempt = System.currentTimeMillis();
        } catch (RestClientException e) {
            lastSendToClientAttempt = System.currentTimeMillis();
            suspendMessageReceiver();
            if (log.isDebugEnabled()) {
                log.debug("Unsuccessful call to client", e);
            }
            throw new TransferException(e);
        }
    }

    @Override
    public void suspendMessageReceiver() {
        log.debug("Suspending message listener at [" + String.valueOf(lastSendToClientAttempt) + "]");

        sendToClientFault = true;
        container.stop();
    }

    @Override
    public void resumeMessageReceiver() {
        log.info("Resuming message listener");

        sendToClientFault = false;
        container.start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
