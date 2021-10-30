package com.feedbeforeflight.marshrutka.transport;

public interface MessageBrokerServiceNotificationClient {

    boolean messageReceived(Message message);

}
