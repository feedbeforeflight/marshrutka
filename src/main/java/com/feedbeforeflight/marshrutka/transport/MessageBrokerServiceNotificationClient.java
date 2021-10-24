package com.feedbeforeflight.marshrutka.transport;

public interface MessageBrokerServiceNotificationClient {

    public boolean messageReceived(Message message);

}
