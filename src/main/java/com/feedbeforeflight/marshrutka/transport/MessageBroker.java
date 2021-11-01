package com.feedbeforeflight.marshrutka.transport;

public interface MessageBroker extends MessageBrokerRepository {

    void send(HandledMessage message);
    void send(BrokerPoint source, BrokerPoint destination, String brookName, String message);
    void send(String sourcePointName, String destinationPointName, String brookName, String Message);

    HandledMessage receive(BrokerPoint destination);
    HandledMessage receive(String destinationPointName);

    void registerNotificationClient(MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient);

}
