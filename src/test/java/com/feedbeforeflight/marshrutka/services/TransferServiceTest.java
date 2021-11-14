package com.feedbeforeflight.marshrutka.services;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.transport.*;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@ActiveProfiles("test")
class TransferServiceTest {

    @MockBean MessageBroker messageBroker;
    @MockBean MessageBrokerManager messageBrokerManager;

    @Autowired
    TransferService transferService;

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void contextLoads() {
        assertThat(transferService, notNullValue());

        Mockito.verify(messageBrokerManager, Mockito.times(1)).applyConfiguration();
        Mockito.verify(messageBroker, Mockito.times(1)).registerNotificationClient(transferService);
    }

    @Test
    void sendDirect_WithStandardMessage_ShouldSucceed() {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));
        HandledMessage testMessage = new HandledMessage(point1, point2, "testFlow", "testMessage");

        transferService.sendDirect(testMessage);

        Mockito.verify(messageBroker, Mockito.times(1)).send(testMessage);
    }

    @Test
    void sendDirect_WithExplodedMessage_ShouldSucceed() {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));
        String testMessage = "test message";
        String testFlowName= "testflow";

        Mockito.when(messageBroker.createHandledMessage(point1.getName(), point2.getName(), testFlowName, testMessage)).thenReturn(
                new HandledMessage(point1, point2, testFlowName, testMessage));

        transferService.sendDirect(point1, point2, testFlowName, testMessage);

        ArgumentCaptor<HandledMessage> messageCaptor = ArgumentCaptor.forClass(HandledMessage.class);

        Mockito.verify(messageBroker, Mockito.times(1)).send(messageCaptor.capture());
        assertThat(messageCaptor.getAllValues(), hasSize(1));
        HandledMessage capturedMessage = messageCaptor.getValue();
        assertThat(capturedMessage.getSource(), equalTo(point1));
        assertThat(capturedMessage.getDestination(), equalTo(point2));
        assertThat(capturedMessage.getFlowName(), equalTo(testFlowName));
        assertThat(capturedMessage.getPayload(), equalTo(testMessage));
    }

    @Test
    void receive_WithExistingPoint_ShouldReturnHandledMessage() {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));
        String testMessage = "test message";
        String testFlowName= "testflow";

        Mockito.when(messageBroker.receive(point2)).thenReturn(
                new HandledMessage(point1, point2, testFlowName, testMessage));

        HandledMessage receivedMessage = transferService.receive(point2);

        assertThat(receivedMessage, notNullValue());
        assertThat(receivedMessage.getSource(), equalTo(point1));
        assertThat(receivedMessage.getDestination(), equalTo(point2));
        assertThat(receivedMessage.getFlowName(), equalTo(testFlowName));
        assertThat(receivedMessage.getPayload(), equalTo(testMessage));
    }

    @Test
    void getPoint_WithExistingId_ShouldReturnBrokerPoint() {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        Mockito.when(messageBroker.getPoint("1")).thenReturn(java.util.Optional.of(point1));

        BrokerPoint returnedPoint = transferService.getPoint("1");

        assertThat(returnedPoint, notNullValue());
        assertThat(returnedPoint, sameInstance(point1));
    }

    @Test
    void getPoint_WithNonExistingId_ShouldReturnNull() {
        BrokerPoint returnedPoint = transferService.getPoint("2");

        assertThat(returnedPoint, nullValue());
    }

    @Test
    void messageReceived_WithStandardCall_ShouldReturnFalse() {
        assertThat(transferService.messageReceived(null), is(false));
    }
}