package com.feedbeforeflight.marshrutka.transport;

import java.util.Optional;

public interface MessageBrokerRepository {

    public Optional<BrokerPoint> getPoint(String name);

    public Message wrapMessage(String sourcePointName, String destinationPointName, String payload);

}
