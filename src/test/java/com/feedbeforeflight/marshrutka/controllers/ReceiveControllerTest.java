package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import com.feedbeforeflight.marshrutka.services.TransferService;
import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.HandledMessage;
import com.feedbeforeflight.marshrutka.transport.RabbitBrokerPoint;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiveController.class)
class ReceiveControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    TransferService transferService;

    @Test
    void receive_WithCorrectPoint_ShouldReturnMessage() throws Exception {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));

        String testMessage = "test message";
        HandledMessage handledMessage = new HandledMessage(null, point1, "", testMessage);

        Mockito.when(transferService.getPoint("1")).thenReturn(point1);
        Mockito.when(transferService.receive(point1)).thenReturn(handledMessage);

        mockMvc.perform(get("/receive/1"))
                .andDo(print())
                .andExpectAll(
                        status().isOk(),
                        content().string(testMessage)
                );
    }

    @Test
    void receive_WithCorrectPoint_ShouldReturnNoMessages() throws Exception {
        BrokerPoint point1 = new RabbitBrokerPoint(null, null);
        point1.init(new PointEntity(1, "1", true));

        Mockito.when(transferService.getPoint("1")).thenReturn(point1);
        Mockito.when(transferService.receive(point1)).thenReturn(null);

        mockMvc.perform(get("/receive/1"))
                .andDo(print())
                .andExpectAll(
                        status().isNoContent(),
                        content().string("No messages to receive")
                );
    }

    @Test
    void receive_WithIncorrectPoint_ShouldReturn404WithPointNotFound() throws Exception {
        mockMvc.perform(get("/receive/2"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("Point not found")
                );
    }

    @Test
    void receive_WithoutPoint_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/receive/"))
                .andDo(print())
                .andExpectAll(
                        status().isNotFound(),
                        content().string("")
                );
    }
}