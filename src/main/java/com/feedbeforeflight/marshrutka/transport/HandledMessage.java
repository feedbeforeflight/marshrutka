package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class HandledMessage {

    @Getter @Setter private BrokerPoint source;
    @Getter @Setter private BrokerPoint destination;
    @Getter @Setter private String brookName;

    @Getter private String payload;
}
