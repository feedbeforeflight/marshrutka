package com.feedbeforeflight.marshrutka.transport;

public interface MessageBroker extends MessageBrokerRepository {

    void send(Message message);
    void send(BrokerPoint source, BrokerPoint destination, String message);
    void send(String sourcePointName, String destinationPointName, String Message);

    Message receive(BrokerPoint destination);
    Message receive(String destinationPointName);

    void registerNotificationClient(MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient);

}
