package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.services.TransferService;
import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.RabbitBrokerPoint;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SendController.class)
class SendControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransferService transferService;

    @Test
    void sendDefault_WithAnyRequest_ShouldReturnNotImplemented() throws Exception {
        mockMvc.perform(post("/send/1"))
                .andDo(print())
                .andExpect(status().isNotImplemented());
    }

    @Test
    void sendDirect_WithCorrectRequest_ShouldSucceed() throws Exception {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));

        Mockito.when(transferService.getPoint("1")).thenReturn(point1);
        Mockito.when(transferService.getPoint("2")).thenReturn(point2);

        String testMessage = "test message";
        String testFlowName= "testflow";

        mockMvc.perform(post("/send/1/direct/2")
                        .content(testMessage)
                        .header("X-flow-name", testFlowName))
                .andDo(print())
                .andExpectAll(
                        status().isAccepted(),
                        content().string("Sent")
                );

        ArgumentCaptor<BrokerPoint> pointArgumentCaptorSource = ArgumentCaptor.forClass(BrokerPoint.class);
        ArgumentCaptor<BrokerPoint> pointArgumentCaptorDestination = ArgumentCaptor.forClass(BrokerPoint.class);
        ArgumentCaptor<String> messageArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> flowNameArgumentCaptor = ArgumentCaptor.forClass(String.class);

        Mockito.verify(transferService, Mockito.times(1)).getPoint("1");
        Mockito.verify(transferService, Mockito.times(1)).getPoint("2");

        Mockito.verify(transferService, Mockito.times(1)).sendDirect(
                pointArgumentCaptorSource.capture(),
                pointArgumentCaptorDestination.capture(),
                flowNameArgumentCaptor.capture(),
                messageArgumentCaptor.capture());
        assertThat(pointArgumentCaptorSource.getAllValues(), hasSize(1));
        BrokerPoint capturedPoint1 = pointArgumentCaptorSource.getValue();
        assertThat("Source point differs", capturedPoint1, sameInstance(point1));

        assertThat(pointArgumentCaptorDestination.getAllValues(), hasSize(1));
        BrokerPoint capturedPoint2 = pointArgumentCaptorDestination.getValue();
        assertThat("Destination point differs", capturedPoint2, sameInstance(point2));

        assertThat(flowNameArgumentCaptor.getAllValues(), hasSize(1));
        String capturedFlowName = flowNameArgumentCaptor.getValue();
        assertThat("Incorrect flow name captured", capturedFlowName, equalTo(testFlowName));

        assertThat(messageArgumentCaptor.getAllValues(), hasSize(1));
        String capturedMessage = messageArgumentCaptor.getValue();
        assertThat("Incorrect message captured", capturedMessage, equalTo(testMessage));
    }

    @Test
    void sendDirect_WithIncorrectSender_ShouldReturn404WithPointNotFound() throws Exception {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));

        Mockito.when(transferService.getPoint("1")).thenReturn(point1);
        Mockito.when(transferService.getPoint("2")).thenReturn(point2);

        String testMessage = "test message";
        String testFlowName= "testflow";

        mockMvc.perform(post("/send/3/direct/2")
                        .content(testMessage)
                        .header("X-flow-name", testFlowName))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("Source point not found")
                );
    }

    @Test
    void sendDirect_WithIncorrectReceiver_ShouldReturn404WithPointNotFound() throws Exception {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));
        BrokerPoint point2 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(2, "2", true));

        Mockito.when(transferService.getPoint("1")).thenReturn(point1);
        Mockito.when(transferService.getPoint("2")).thenReturn(point2);

        String testMessage = "test message";
        String testFlowName= "testflow";

        mockMvc.perform(post("/send/1/direct/3")
                        .content(testMessage)
                        .header("X-flow-name", testFlowName))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("Destination point not found")
                );
    }

    @Test
    void sendDirect_WithoutSender_ShouldReturn404WithSenderNotSpecified() throws Exception {
        String testMessage = "test message";
        String testFlowName= "testflow";

        mockMvc.perform(post("/send//direct/2")
                        .content(testMessage)
                        .header("X-flow-name", testFlowName))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("")
                );
    }

    @Test
    void sendDirect_WithoutReceiver_ShouldReturn404WithReceiverNotSpecified() throws Exception {
        String testMessage = "test message";
        String testFlowName= "testflow";

        mockMvc.perform(post("/send/1/direct/")
                        .content(testMessage)
                        .header("X-flow-name", testFlowName))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("")
                );
    }

}