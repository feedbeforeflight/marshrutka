package com.feedbeforeflight.marshrutka.transport;

import java.util.List;

public interface MessageBrokerManager {

    void applyConfiguration();

    List<BrokerPoint> getPointList();

}
