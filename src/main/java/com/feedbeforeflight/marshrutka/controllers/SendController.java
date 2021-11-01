package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.services.TransferException;
import com.feedbeforeflight.marshrutka.services.TransferService;
import com.feedbeforeflight.marshrutka.transport.BrokerPoint;
import com.feedbeforeflight.marshrutka.transport.HandledMessage;
import com.feedbeforeflight.marshrutka.transport.MessageBrokerRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/send")
public class SendController {

    private final TransferService transferService;
    private final MessageBrokerRepository messageBrokerRepository;

    public SendController(TransferService transferService, MessageBrokerRepository messageBrokerRepository) {
        this.transferService = transferService;
        this.messageBrokerRepository = messageBrokerRepository;
    }

    @PostMapping("/{sourceName}")
    public ResponseEntity<String> sendDefault(@PathVariable(name = "sourceName") String sourceName) {

        return new ResponseEntity<>(sourceName, HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/{sourceName}/direct/{destinationName}")
    public ResponseEntity<String> sendDirect(
            @PathVariable(name = "sourceName") String sourceName,
            @PathVariable(name = "destinationName") String destinationName,
            @RequestHeader(name = "X-brook-name") String brookName,
            @RequestBody String requestBody) {

        Optional<BrokerPoint> sourcePoint = messageBrokerRepository.getPoint(sourceName);
        if (sourcePoint.isEmpty()) {
            return new ResponseEntity<>("Source point not found", HttpStatus.NOT_FOUND);
        }
        Optional<BrokerPoint> destinationPoint = messageBrokerRepository.getPoint(destinationName);
        if (destinationPoint.isEmpty()) {
            return new ResponseEntity<>("Destination point not found", HttpStatus.NOT_FOUND);
        }

        // todo: should validate brook name here. could be made after adding brook persistence
        HandledMessage message = new HandledMessage(sourcePoint.get(), destinationPoint.get(), brookName, requestBody);

        try {
            transferService.sendDirect(message);
        }
        catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Sent", HttpStatus.ACCEPTED);
    }

}
