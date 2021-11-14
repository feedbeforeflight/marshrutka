package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.HandledMessage;
import com.feedbeforeflight.marshrutka.services.TransferException;
import com.feedbeforeflight.marshrutka.services.TransferService;
import com.feedbeforeflight.marshrutka.transport.MessageBrokerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/receive")
public class ReceiveController {

    private final TransferService transferService;

    public ReceiveController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/{destinationName}")
    public ResponseEntity<String> receive(@PathVariable(name = "destinationName") String name) {
        BrokerPoint brokerPoint = transferService.getPoint(name);
        if (brokerPoint == null) {
            return new ResponseEntity<>("Point not found", HttpStatus.NOT_FOUND);
        }

        try {
            HandledMessage message = transferService.receive(brokerPoint);
            if (message == null) {
                return new ResponseEntity<>("No messages to receive", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(message.getPayload(), HttpStatus.OK);
        } catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
