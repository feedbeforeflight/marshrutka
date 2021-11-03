package com.feedbeforeflight.marshrutka.transport;

import java.util.Optional;

public interface MessageBrokerRepository {

    Optional<BrokerPoint> getPoint(String name);
    Optional<BrokerPoint> getPointByID(int id);

    HandledMessage createHandledMessage(String sourcePointName, String destinationPointName, String flowName, String payload);

}
