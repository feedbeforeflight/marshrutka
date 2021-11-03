package com.feedbeforeflight.marshrutka.transport;

import com.feedbeforeflight.marshrutka.models.PointLiveData;

import java.util.List;

public interface MessageBrokerManager {

    void applyConfiguration();

    List<BrokerPoint> getPointList();

    PointLiveData getPointLiveData(int pointID);
    List<PointLiveData> getLiveData();

}
