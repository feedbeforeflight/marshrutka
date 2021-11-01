package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;

public interface BrokerPoint {

    int getId();
    String getName();

    boolean receiveSuspended();
    long getLastSendToClientAttempt();

    void init(PointEntity pointEntity);
    void powerOff();

    void suspendMessageReceiver();
    void resumeMessageReceiver();

    void messageReceived(String message, String brookName);
}
