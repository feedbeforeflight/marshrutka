package com.feedbeforeflight.marshrutka.controllers;

import com.feedbeforeflight.marshrutka.dao.PointRepository;
import com.feedbeforeflight.marshrutka.models.Message;
import com.feedbeforeflight.marshrutka.models.Point;
import com.feedbeforeflight.marshrutka.services.TransferException;
import com.feedbeforeflight.marshrutka.services.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receive")
public class ReceiveController {

    private final TransferService transferService;

    public ReceiveController(TransferService transferService) {
        this.transferService = transferService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> receive(@PathVariable(name = "id") int id) {
        Point point = transferService.getPoint(id);
        if (point == null) {
            return new ResponseEntity<>("Point not found", HttpStatus.NOT_FOUND);
        }

        try {
            Message message = transferService.receive(point);
            if (message == null) {
                return new ResponseEntity<>("No messages to receive", HttpStatus.NO_CONTENT);
            }
            return new ResponseEntity<>(message.getPayload(), HttpStatus.OK);
        } catch (TransferException transferException) {
            return new ResponseEntity<>(transferException.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
