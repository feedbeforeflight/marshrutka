package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.services.TransferService;
import com.feedbeforeflight.marshrutka.transport.MessageBrokerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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

    @MockBean
    MessageBrokerRepository messageBrokerRepository;

    @Test
    void sendDefault() throws Exception {
        mockMvc.perform(post("/send/1"))
                .andDo(print())
                .andExpect(status().isNotImplemented());
    }

    @Test
    void sendDirect() throws Exception {
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
    }
}