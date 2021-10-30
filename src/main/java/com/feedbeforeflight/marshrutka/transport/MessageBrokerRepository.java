package com.feedbeforeflight.marshrutka.transport;

import java.util.Optional;

public interface MessageBrokerRepository {

    Optional<BrokerPoint> getPoint(String name);

    Message wrapMessage(String sourcePointName, String destinationPointName, String payload);

}
