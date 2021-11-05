package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointEntity;

public interface BrokerPoint {

    int getId();
    String getName();
    boolean isActive();

    int getMessagesSent();
    int getMessagesReceived();
    int getMessagesQueued();

    boolean receiveSuspended();
    long getLastSendToClientAttempt();

    void init(PointEntity pointEntity);
    void powerOffListener();

    void suspendMessageReceiver();
    void resumeMessageReceiver();

    void messageReceived(String message, String flowName);

    void increaseSentCount();
    void updateQueuedCount();
}
