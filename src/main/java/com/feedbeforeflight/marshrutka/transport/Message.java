package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class Message {

    @Getter
    @Setter
    private BrokerPoint source;
    @Getter
    @Setter
    private BrokerPoint destination;

    @Getter
    private String payload;
}
