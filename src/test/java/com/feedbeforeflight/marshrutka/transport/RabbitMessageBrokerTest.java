package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.models.PointLiveData;
import com.feedbeforeflight.marshrutka.services.TransferException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@ExtendWith(SpringExtension.class)
class RabbitMessageBrokerTest {

    @MockBean
    AmqpTemplate amqpTemplate;
    @MockBean
    AmqpAdmin amqpAdmin;
    @MockBean
    PointRepository pointRepository;
    @MockBean
    ApplicationContext context;

    @BeforeEach
    void setUp() {
        List<PointEntity> entityList = new ArrayList<>();
        entityList.add(new PointEntity(1, "Point1", true));
        entityList.add(new PointEntity(2, "Point2", true));
        entityList.add(new PointEntity(3, "Point3", true));
        entityList.add(new PointEntity(4, "Point4", false));
        entityList.add(new PointEntity(5, "Point5", false));
        Mockito.when(pointRepository.findAll()).thenReturn(entityList);
        Mockito.when(context.getBean(BrokerPoint.class)).thenAnswer(I -> new RabbitBrokerPoint(amqpTemplate, amqpAdmin));
    }

    @AfterEach
    void tearDown() {

    }

    private RabbitMessageBroker createAndInitMessageBroker() {
        RabbitMessageBroker messageBroker = new RabbitMessageBroker(pointRepository, amqpTemplate, amqpAdmin);
        messageBroker.setApplicationContext(context);
        messageBroker.applyConfiguration();

        return messageBroker;
    }

    @Test
    void getPoint_WhenPointNameFound_ShouldReturnBrokerPointOptional() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        Optional<BrokerPoint> point = messageBroker.getPoint("Point2");

        assertThat(point.isEmpty(), is(false));
        BrokerPoint foundPoint = point.get();
        assertThat(foundPoint.getName(), equalTo("Point2"));
        assertThat(foundPoint.getId(), equalTo(2));
        assertThat(foundPoint.isActive(), is(true));
    }

    @Test
    void getPoint_WhenPointNameNotFound_ShouldReturnNulledOptional() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.setApplicationContext(context);
        messageBroker.applyConfiguration();

        Optional<BrokerPoint> point = messageBroker.getPoint("Point");

        assertThat(point.isEmpty(), is(true));
    }

    @Test
    void getPointByID_WhenPointIDFound_ShouldReturnBrokerPointOptional() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.setApplicationContext(context);
        messageBroker.applyConfiguration();

        Optional<BrokerPoint> point = messageBroker.getPointByID(2);

        assertThat(point.isEmpty(), is(false));
        BrokerPoint foundPoint = point.get();
        assertThat(foundPoint.getName(), equalTo("Point2"));
        assertThat(foundPoint.getId(), equalTo(2));
        assertThat(foundPoint.isActive(), is(true));
    }

    @Test
    void getPointByID_WhenPointIDNotFound_ShouldReturnNulledOptional() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.setApplicationContext(context);
        messageBroker.applyConfiguration();

        Optional<BrokerPoint> point = messageBroker.getPointByID(25);

        assertThat(point.isEmpty(), is(true));
    }

    @Test
    void createHandledMessage_WhenCalled_ShouldReturnHandledMessageInstance() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        HandledMessage handledMessage = messageBroker.createHandledMessage(
                "Point1",
                "Point2",
                "TestFlow",
                "Test payload");

        assertThat(handledMessage, notNullValue());
        assertThat(handledMessage.getSource().getName(), equalTo("Point1"));
        assertThat(handledMessage.getDestination().getName(), equalTo("Point2"));
        assertThat(handledMessage.getFlowName(), equalTo("TestFlow"));
        assertThat(handledMessage.getPayload(), equalTo("Test payload"));
    }

    @Test
    void send_WhenCalledWithHandledMessageAndDestination_ShouldSendToRabbitDirectToQueue() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        HandledMessage handledMessage = messageBroker.createHandledMessage(
                "Point1",
                "Point2",
                "TestFlow",
                "Test payload");

        assertThat(handledMessage.getSource().getMessagesSent(), equalTo(0));
        messageBroker.send(handledMessage);

        ArgumentCaptor<String> destinationNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(amqpTemplate, Mockito.times(1)).convertAndSend(
                destinationNameCaptor.capture(),
                (Object) payloadCaptor.capture(),
                postProcessorCaptor.capture());
        assertThat(handledMessage.getSource().getMessagesSent(), equalTo(1));

        assertThat(destinationNameCaptor.getValue(), equalTo("Point2"));
        assertThat(payloadCaptor.getValue(), equalTo("Test payload"));
        assertThat(postProcessorCaptor.getAllValues(), hasSize(1));
        assertThat(postProcessorCaptor.getValue(), isA(MessagePostProcessor.class));
    }

    @Test
    void send_WhenCalledWithHandledMessageWithoutDestination_ShouldSendToRabbitToExchange() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        HandledMessage handledMessage = messageBroker.createHandledMessage(
                "Point1",
                "",
                "TestFlow",
                "Test payload");

        assertThat(handledMessage.getSource().getMessagesSent(), equalTo(0));
        messageBroker.send(handledMessage);

        ArgumentCaptor<String> sourceNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> destinationNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(amqpTemplate, Mockito.times(1)).convertAndSend(
                sourceNameCaptor.capture(),
                destinationNameCaptor.capture(),
                (Object) payloadCaptor.capture(),
                postProcessorCaptor.capture());
        assertThat(handledMessage.getSource().getMessagesSent(), equalTo(1));

        assertThat(sourceNameCaptor.getValue(), equalTo("Point1"));
        assertThat(destinationNameCaptor.getValue(), equalTo(""));
        assertThat(payloadCaptor.getValue(), equalTo("Test payload"));
        assertThat(postProcessorCaptor.getAllValues(), hasSize(1));
        assertThat(postProcessorCaptor.getValue(), isA(MessagePostProcessor.class));
    }

    @Test
    void send_WhenCalledWithHandledMessageWithInactiveSource_ShouldTrowException() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        HandledMessage handledMessage = messageBroker.createHandledMessage(
                "Point4",
                "Point5",
                "TestFlow",
                "Test payload");

        TransferException transferException = assertThrows(TransferException.class, () -> messageBroker.send(handledMessage));
        assertThat(transferException.getMessage(), startsWith("Source point"));
        assertThat(transferException.getMessage(), containsString("Point4"));
        assertThat(transferException.getMessage(), endsWith("is not active"));
    }

    @Test
    void send_WhenCalledWithHandledMessageWithInactiveDestination_ShouldThrowException() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        HandledMessage handledMessage = messageBroker.createHandledMessage(
                "Point1",
                "Point5",
                "TestFlow",
                "Test payload");

        TransferException transferException = assertThrows(TransferException.class, () -> messageBroker.send(handledMessage));
        assertThat(transferException.getMessage(), startsWith("Destination point"));
        assertThat(transferException.getMessage(), containsString("Point5"));
        assertThat(transferException.getMessage(), endsWith("is not active"));
    }

    @Test
    void Send_WhenCalledWithPoints_ShouldSendToRabbitDirectToQueue() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.send(
                messageBroker.getPoint("Point1").get(),
                messageBroker.getPoint("Point2").get(),
                "TestFlow",
                "Test payload");

        ArgumentCaptor<String> destinationNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(amqpTemplate, Mockito.times(1)).convertAndSend(
                destinationNameCaptor.capture(),
                (Object) payloadCaptor.capture(),
                postProcessorCaptor.capture());

        assertThat(destinationNameCaptor.getValue(), equalTo("Point2"));
        assertThat(payloadCaptor.getValue(), equalTo("Test payload"));
        assertThat(postProcessorCaptor.getAllValues(), hasSize(1));
        assertThat(postProcessorCaptor.getValue(), isA(MessagePostProcessor.class));
    }

    @Test
    void Send_WhenCalledWithPointNames_ShouldSendToRabbitDirectToQueue() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.send(
                "Point1",
                "Point2",
                "TestFlow",
                "Test payload");

        ArgumentCaptor<String> destinationNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> payloadCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> postProcessorCaptor = ArgumentCaptor.forClass(MessagePostProcessor.class);
        Mockito.verify(amqpTemplate, Mockito.times(1)).convertAndSend(
                destinationNameCaptor.capture(),
                (Object) payloadCaptor.capture(),
                postProcessorCaptor.capture());

        assertThat(destinationNameCaptor.getValue(), equalTo("Point2"));
        assertThat(payloadCaptor.getValue(), equalTo("Test payload"));
        assertThat(postProcessorCaptor.getAllValues(), hasSize(1));
        assertThat(postProcessorCaptor.getValue(), isA(MessagePostProcessor.class));
    }

    @Test
    void receive_WhenCalledWithBrokerPoint_ShouldReturnHandledMessage() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();
        Mockito.when(amqpTemplate.receiveAndConvert("Point1")).thenReturn("Test payload");

        HandledMessage handledMessage = messageBroker.receive(messageBroker.getPoint("Point1").get());

        assertThat(handledMessage, notNullValue());
        assertThat(handledMessage.getSource(), nullValue());
        assertThat(handledMessage.getDestination(), notNullValue());
        assertThat(handledMessage.getDestination().getName(), equalTo("Point1"));
        assertThat(handledMessage.getPayload(), equalTo("Test payload"));
        assertThat(handledMessage.getFlowName(), emptyString());
    }

    @Test
    void receive_WhenCalledWithExistingBrokerPointName_ShouldReturnHandledMessage() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();
        Mockito.when(amqpTemplate.receiveAndConvert("Point1")).thenReturn("Test payload");

        HandledMessage handledMessage = messageBroker.receive("Point1");

        assertThat(handledMessage, notNullValue());
        assertThat(handledMessage.getSource(), nullValue());
        assertThat(handledMessage.getDestination(), notNullValue());
        assertThat(handledMessage.getDestination().getName(), equalTo("Point1"));
        assertThat(handledMessage.getPayload(), equalTo("Test payload"));
        assertThat(handledMessage.getFlowName(), emptyString());
    }

    @Test
    void receive_WhenCalledWithNonExistingBrokerPointName_ShouldRaiseTransferException() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();
        Mockito.when(amqpTemplate.receiveAndConvert("Point1")).thenReturn("Test payload");

        HandledMessage handledMessage;
        TransferException transferException = assertThrows(TransferException.class,
                () -> messageBroker.receive("Point25"));

        assertThat(transferException.getMessage(), startsWith("Destination point"));
        assertThat(transferException.getMessage(), containsString("Point25"));
        assertThat(transferException.getMessage(), endsWith("not found"));
    }

    @Test
    void applyConfiguration_WhenCalled_ShouldApplyConfiguration() {
        RabbitMessageBroker messageBroker = new RabbitMessageBroker(pointRepository, amqpTemplate, amqpAdmin);
        messageBroker.setApplicationContext(context);
        messageBroker.applyConfiguration();

        assertThat(messageBroker.getPointList(), hasSize(5));
        assertThat(messageBroker.getPointByID(1).get().getName(), equalTo("Point1"));
        assertThat(messageBroker.getPointByID(1).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(2).get().getName(), equalTo("Point2"));
        assertThat(messageBroker.getPointByID(2).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(3).get().getName(), equalTo("Point3"));
        assertThat(messageBroker.getPointByID(3).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(4).get().getName(), equalTo("Point4"));
        assertThat(messageBroker.getPointByID(4).get().isActive(), is(false));
        assertThat(messageBroker.getPointByID(5).get().getName(), equalTo("Point5"));
        assertThat(messageBroker.getPointByID(5).get().isActive(), is(false));
    }

    @Test
    void getPointList_WhenCalled_ShouldReturnPointList() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        assertThat(messageBroker.getPointList(), hasSize(5));
        assertThat(messageBroker.getPointByID(1).get().getName(), equalTo("Point1"));
        assertThat(messageBroker.getPointByID(1).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(2).get().getName(), equalTo("Point2"));
        assertThat(messageBroker.getPointByID(2).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(3).get().getName(), equalTo("Point3"));
        assertThat(messageBroker.getPointByID(3).get().isActive(), is(true));
        assertThat(messageBroker.getPointByID(4).get().getName(), equalTo("Point4"));
        assertThat(messageBroker.getPointByID(4).get().isActive(), is(false));
        assertThat(messageBroker.getPointByID(5).get().getName(), equalTo("Point5"));
        assertThat(messageBroker.getPointByID(5).get().isActive(), is(false));
    }

    @Test
    void getPointLiveData_WhenCalled_ShouldReturnLiveData() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.send("Point1","Point2","TestFlow","Test payload");
        messageBroker.send("Point1","Point2","TestFlow","Test payload 2");
        HandledMessage handledMessage = messageBroker.receive("Point1");

        PointLiveData liveData = messageBroker.getPointLiveData(1);
        assertThat(liveData.getMessagesQueued(), equalTo(0));
        assertThat(liveData.getMessagesSent(), equalTo(2));
        assertThat(liveData.getMessagesReceived(), equalTo(0));

        liveData = messageBroker.getPointLiveData(2);
        assertThat(liveData.getMessagesQueued(), equalTo(0));
        assertThat(liveData.getMessagesSent(), equalTo(0));
        assertThat(liveData.getMessagesReceived(), equalTo(0));
    }

    @Test
    void getLiveData_WhenCalled_ShouldReturnLiveDataList() {
        RabbitMessageBroker messageBroker = createAndInitMessageBroker();

        messageBroker.send("Point1","Point2","TestFlow","Test payload");
        messageBroker.send("Point1","Point2","TestFlow","Test payload 2");
        HandledMessage handledMessage = messageBroker.receive("Point1");

        List<PointLiveData> liveDataList = messageBroker.getLiveData();

        PointLiveData liveData = liveDataList.get(0);
        assertThat(liveData.getMessagesQueued(), equalTo(0));
        assertThat(liveData.getMessagesSent(), equalTo(2));
        assertThat(liveData.getMessagesReceived(), equalTo(0));

        liveData = liveDataList.get(1);
        assertThat(liveData.getMessagesQueued(), equalTo(0));
        assertThat(liveData.getMessagesSent(), equalTo(0));
        assertThat(liveData.getMessagesReceived(), equalTo(0));
    }
}