package com.feedbeforeflight.marshrutka.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Message {

    @Getter
    @Setter
    private Point source;
    @Getter
    @Setter
    private Point destination;

    @Getter
    @Setter
    private String payload;
}
