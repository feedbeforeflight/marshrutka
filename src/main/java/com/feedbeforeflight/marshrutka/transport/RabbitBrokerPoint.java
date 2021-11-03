package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.models.PointEntityProtocol;
import com.feedbeforeflight.marshrutka.rabbitmq.RabbitMessageReceiver;
import com.feedbeforeflight.marshrutka.services.TransferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class RabbitBrokerPoint implements BrokerPoint, ApplicationContextAware {

    @Getter boolean active = false;
    @Getter private int id;
    @Getter private String name;
    @Getter private String pushURL;
    @Getter private boolean pushEnabled;
    @Getter private PointEntityProtocol pushProtocol;

    private final AtomicInteger messagesSent = new AtomicInteger(0);
    private final AtomicInteger messagesReceived = new AtomicInteger(0);
    private final AtomicInteger messagesQueued = new AtomicInteger(0);

    private ApplicationContext applicationContext;
    private final AmqpTemplate amqpTemplate;
    private final AmqpAdmin amqpAdmin;

    @Getter private SimpleMessageListenerContainer container;

    private Queue queue;

    @Getter private long lastSendToClientAttempt = 0;
    @Getter boolean sendToClientFault = false;

    @Override
    public int getMessagesSent() {
        return messagesSent.get();
    }

    @Override
    public int getMessagesReceived() {
        return messagesReceived.get();
    }

    @Override
    public int getMessagesQueued() {
        return messagesQueued.get();
    }

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
        this.pushURL = pointEntity.getPushURL();
        this.active = pointEntity.isActive();
        this.pushEnabled = pointEntity.isPushEnabled();

        if (!active || !pushEnabled) {
            powerOffListener();
            return;
        };

        log.debug("Starting listener container for receive. Point " + name);

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
    public void powerOffListener() {
        if (container == null) { return; }

        log.debug("Powering off listener container for receive. Point " + name);

        //amqpAdmin.deleteQueue(this.queue.getName()); // maybe dropping potentially not empty queue is not a good idea
        container.stop();
        container.shutdown();
        container.removeQueueNames(this.queue.getName());
        container = null;
    }

    @Override
    public void messageReceived(String message, String flowName) throws TransferException {
        log.debug("Receiving message. flow name: " + flowName);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "text/html; charset=utf-8");
        headers.set("X-flow-name", flowName);

        HttpEntity<String> request = new HttpEntity<>(message, headers);

        try {
            String result = restTemplate.postForObject(pushURL, request, String.class);
            lastSendToClientAttempt = System.currentTimeMillis();
            messagesReceived.incrementAndGet();
        } catch (RestClientException e) {
            lastSendToClientAttempt = System.currentTimeMillis();
            log.error("Unsuccessful call to client", e);
            suspendMessageReceiver();
            throw new TransferException(e);
        }
    }

    @Override
    public synchronized void increaseSentCount() {
        messagesSent.incrementAndGet();
    }

    @Override
    public void updateQueuedCount() {
        if (!active) {
            return;
        }

        QueueInformation queueInformation = amqpAdmin.getQueueInfo(queue.getName());
        messagesQueued.set(queueInformation == null ? 0 : queueInformation.getMessageCount());
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
