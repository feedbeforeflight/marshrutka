package com.feedbeforeflight.marshrutka.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/heartbeat")
public class HeartbeatController {

    @GetMapping("/{id}")
    public String heartbeat() {

        return "";
    }

}
