package com.feedbeforeflight.marshrutka.models;

public interface ConnectablePoint {

    public String SendDirectly(ConnectablePoint destination);

    public String Receive();

    public boolean HasMessages();


}
