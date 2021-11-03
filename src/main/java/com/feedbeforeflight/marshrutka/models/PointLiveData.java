package com.feedbeforeflight.marshrutka.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class PointLiveData {

    @Getter private int pointID;
    @Getter private int messagesSent;
    @Getter private int messagesReceived;
    @Getter private int messagesQueued;
    @Getter private boolean pushErroneous;

}
