package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.models.Message;
import com.feedbeforeflight.marshrutka.models.Point;
import com.feedbeforeflight.marshrutka.services.TransferException;
import com.feedbeforeflight.marshrutka.services.TransferService;
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

    @PostMapping("/{id}")
    public ResponseEntity<String> sendDefault(@PathVariable(name = "id") int id) {

        return new ResponseEntity<>("", HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/{srcId}/direct/{destId}")
    public ResponseEntity<String> sendDirect(
            @PathVariable(name = "srcId") int srcId,
            @PathVariable(name = "destId") int destId,
            @RequestBody String requestBody) {

        Point sourcePoint = transferService.getPoint(srcId);
        if (sourcePoint == null) {
            return new ResponseEntity<>("Source point not found", HttpStatus.NOT_FOUND);
        }
        Point destinationPoint = transferService.getPoint(srcId);
        if (destinationPoint == null) {
            return new ResponseEntity<>("Destination point not found", HttpStatus.NOT_FOUND);
        }

        Message message = new Message(sourcePoint, destinationPoint, requestBody);

        try {
            transferService.sendDirect(message);
        }
        catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("Sent", HttpStatus.ACCEPTED);
    }

}
