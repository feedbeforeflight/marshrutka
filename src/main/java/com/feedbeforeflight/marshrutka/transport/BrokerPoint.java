package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;

public interface BrokerPoint {

    int getId();
    String getName();

    void init(PointEntity pointEntity);
    void powerOff();
}
