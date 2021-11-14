package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.models.PointEntityProtocol;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
class RabbitBrokerPointTest {

    @MockBean
    AmqpTemplate amqpTemplate;
    @MockBean
    AmqpAdmin amqpAdmin;
    @MockBean
    SimpleMessageListenerContainer container;
    @MockBean
    ApplicationContext context;
    @MockBean
    RestTemplate restTemplate;

    @Test
    void init_WhenInactive_ShouldNotDeclareQueue() {
        PointEntity pointEntity = new PointEntity(1, "TestPoint", false);
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);

        point.init(pointEntity);

        assertThat("Point should be inactive", point.isActive(), is(false));
        assertThat("Wrong point id", point.getId(), equalTo(pointEntity.getId()));
        assertThat("Wrong point name", point.getName(), equalTo(pointEntity.getName()));

        ArgumentCaptor<Queue> queueCaptor = ArgumentCaptor.forClass(Queue.class);

        Mockito.verify(amqpAdmin, Mockito.times(0)).declareQueue(queueCaptor.capture());
        Mockito.verify(container, Mockito.times(0)).start();
    }

    @Test
    void init_WhenActiveWithoutPush_ShouldNotStartListener() {
        PointEntity pointEntity = new PointEntity(1, "TestPoint", true);
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);

        point.init(pointEntity);

        assertThat("Point should be active", point.isActive(), is(true));
        assertThat("Wrong point id", point.getId(), equalTo(pointEntity.getId()));
        assertThat("Wrong point name", point.getName(), equalTo(pointEntity.getName()));

        ArgumentCaptor<Queue> queueCaptor = ArgumentCaptor.forClass(Queue.class);

        Mockito.verify(amqpAdmin, Mockito.times(0)).declareQueue(queueCaptor.capture());
        Mockito.verify(container, Mockito.times(0)).start();
    }

    @Test
    void init_WhenPushEnabled_ShouldStartListener() {
        PointEntity pointEntity = new PointEntity(1, "TestPoint", true);
        pointEntity.setPushEnabled(true);
        pointEntity.setPushURL("TestUrl");
        pointEntity.setPushProtocol(PointEntityProtocol.REST);
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);
        point.setApplicationContext(context);

        Mockito.when(context.getBean(SimpleMessageListenerContainer.class)).thenReturn(container);

        point.init(pointEntity);

        assertThat("Point should be active", point.isActive(), is(true));
        assertThat("Wrong point id", point.getId(), equalTo(pointEntity.getId()));
        assertThat("Wrong point name", point.getName(), equalTo(pointEntity.getName()));
        assertThat("Push should be enabled", point.isPushEnabled(), is(true));
        assertThat("Push protocol is wrong", point.getPushProtocol(), equalTo(pointEntity.getPushProtocol()));
        assertThat("Push Url is wrong", point.getPushURL(), equalTo(pointEntity.getPushURL()));

        ArgumentCaptor<Queue> queueCaptor = ArgumentCaptor.forClass(Queue.class);
        ArgumentCaptor<MessageListenerAdapter> adapterCaptor = ArgumentCaptor.forClass(MessageListenerAdapter.class);

        Mockito.verify(amqpAdmin, Mockito.times(1)).declareQueue(queueCaptor.capture());
        Queue queue = queueCaptor.getValue();
        assertThat(queue, notNullValue());
        assertThat("Wrong queue name", queue.getName(), equalTo(pointEntity.getName()));
        Mockito.verify(container, Mockito.times(1)).setMessageListener(adapterCaptor.capture());
        MessageListenerAdapter adapter = adapterCaptor.getValue();
        assertThat(adapter, notNullValue());
        Mockito.verify(container, Mockito.times(1)).start();
    }

    @Test
    void powerOffListener_WhenListenerActive_ShouldStopContainer() {
        PointEntity pointEntity = new PointEntity(1, "TestPoint", true);
        pointEntity.setPushEnabled(true);
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);
        point.setApplicationContext(context);

        Mockito.when(context.getBean(SimpleMessageListenerContainer.class)).thenReturn(container);

        point.init(pointEntity);

        ArgumentCaptor<MessageListenerAdapter> adapterCaptor = ArgumentCaptor.forClass(MessageListenerAdapter.class);

        Mockito.verify(container, Mockito.times(1)).setMessageListener(adapterCaptor.capture());
        MessageListenerAdapter adapter = adapterCaptor.getValue();
        assertThat(adapter, notNullValue());
        Mockito.verify(container, Mockito.times(1)).start();

        point.powerOffListener();

        Mockito.verify(container, Mockito.times(1)).stop();
    }

    @Test
    void messageReceived() {
        PointEntity pointEntity = new PointEntity(1, "TestPoint", true);
        pointEntity.setPushEnabled(true);
        pointEntity.setPushURL("TestUrl");
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);
        point.setApplicationContext(context);

        String testMessage = "test message";
        String testFlowName= "testflow";

        Mockito.when(context.getBean(SimpleMessageListenerContainer.class)).thenReturn(container);
        Mockito.when(context.getBean(RestTemplate.class)).thenReturn(restTemplate);

        point.init(pointEntity);

        point.messageReceived(testMessage, testFlowName);

        ArgumentCaptor<String> pushUrlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);

        Mockito.verify(restTemplate, Mockito.times(1)).postForObject(
                pushUrlCaptor.capture(),
                requestCaptor.capture(),
                Mockito.eq(String.class));

        String capturedUrl = pushUrlCaptor.getValue();
        assertThat(capturedUrl, equalTo(pointEntity.getPushURL()));

        HttpEntity request = requestCaptor.getValue();
        assertThat(request, notNullValue());
        assertThat(request.getBody().toString(), equalTo(testMessage));
        assertThat(request.getHeaders().get("X-flow-name").toString(), containsString(testFlowName));

    }

    @Test
    void increaseSentCount_WhenCalled_ShouldIncreaseSentCount() {
        RabbitBrokerPoint point = new RabbitBrokerPoint(amqpTemplate, amqpAdmin);

        assertThat(point.getMessagesSent(), equalTo(0));
        point.increaseSentCount();
        assertThat(point.getMessagesSent(), equalTo(1));
    }

    @Test
    void updateQueuedCount() {
    }

    @Test
    void suspendMessageReceiver() {
    }

    @Test
    void resumeMessageReceiver() {
    }

}