package com.feedbeforeflight.marshrutka.transport;

public interface MessageBrokerServiceNotificationClient {

    boolean messageReceived(HandledMessage message);

}
