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

    public SendController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/{sourceName}")
    public ResponseEntity<String> sendDefault(@PathVariable(name = "sourceName") String sourceName) {

        return new ResponseEntity<>(sourceName, HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/{sourceName}/direct/{destinationName}")
    public ResponseEntity<String> sendDirect(
            @PathVariable(name = "sourceName") String sourceName,
            @PathVariable(name = "destinationName") String destinationName,
            @RequestHeader(name = "X-flow-name") String flowName,
            @RequestBody String requestBody) {

        BrokerPoint sourcePoint = transferService.getPoint(sourceName);
        if (sourcePoint == null) {
            return new ResponseEntity<>("Source point not found", HttpStatus.NOT_FOUND);
        }
        BrokerPoint destinationPoint = transferService.getPoint(destinationName);
        if (destinationPoint == null) {
            return new ResponseEntity<>("Destination point not found", HttpStatus.NOT_FOUND);
        }

        try {
            transferService.sendDirect(sourcePoint, destinationPoint, flowName, requestBody);
        }
        catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Sent", HttpStatus.ACCEPTED);
    }

}
