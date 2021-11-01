package com.feedbeforeflight.marshrutka.transport;

import java.util.Optional;

public interface MessageBrokerRepository {

    Optional<BrokerPoint> getPoint(String name);

    HandledMessage createHandledMessage(String sourcePointName, String destinationPointName, String brookName, String payload);

}
