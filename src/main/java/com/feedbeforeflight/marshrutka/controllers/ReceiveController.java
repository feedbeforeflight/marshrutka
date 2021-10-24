package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.Message;
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
    private final MessageBrokerRepository messageBrokerRepository;

    public ReceiveController(TransferService transferService, MessageBrokerRepository messageBrokerRepository) {
        this.transferService = transferService;
        this.messageBrokerRepository = messageBrokerRepository;
    }

    @GetMapping("/{destinationName}")
    public ResponseEntity<String> receive(@PathVariable(name = "destinationName") String name) {
        Optional<BrokerPoint> brokerPoint = messageBrokerRepository.getPoint(name);
        if (brokerPoint.isEmpty()) {
            return new ResponseEntity<>("Point not found", HttpStatus.NOT_FOUND);
        }

        try {
            Message message = transferService.receive(brokerPoint.get());
            if (message == null) {
                return new ResponseEntity<>("No messages to receive", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(message.getPayload(), HttpStatus.OK);
        } catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
