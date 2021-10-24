package com.feedbeforeflight.marshrutka.transport;

public class RabbitBrokerPoint implements BrokerPoint {

    private int id;
    private String name;

    public RabbitBrokerPoint(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

}
