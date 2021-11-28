package com.feedbeforeflight.marshrutka.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HeartbeatController.class)
class HeartbeatControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void heartbeatTest() throws Exception {
        mockMvc.perform(get("/heartbeat/1"))
                .andDo(print())
                .andExpectAll(
                        status().isOk()
                );
    }

}