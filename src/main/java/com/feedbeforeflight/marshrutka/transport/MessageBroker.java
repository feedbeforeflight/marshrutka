package com.feedbeforeflight.marshrutka.transport;

public interface MessageBroker extends MessageBrokerRepository {

    public boolean send(Message message);
    public boolean send(BrokerPoint source, BrokerPoint destination, String message);
    public boolean send(String sourcePointName, String destinationPointName, String Message);

    public Message receive(BrokerPoint destination);
    public Message receive(String destinationPointName);

    public void registerNotificationClient(MessageBrokerServiceNotificationClient messageBrokerServiceNotificationClient);

}
