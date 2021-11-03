package com.feedbeforeflight.marshrutka.transport;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class HandledMessage {

    @Getter @Setter private BrokerPoint source;
    @Getter @Setter private BrokerPoint destination;
    @Getter @Setter private String flowName;

    @Getter private String payload;
}
