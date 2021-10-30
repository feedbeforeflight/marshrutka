package com.feedbeforeflight.marshrutka.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "Points")
@AllArgsConstructor
public class PointEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "receive_url")
    private String receiveURL;

    public PointEntity() {
        System.out.println("Constructed point entity");
    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }

    public int getId() { return id; }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getReceiveURL() {
        return receiveURL;
    }

    public void setReceiveURL(String receiveURL) {
        this.receiveURL = receiveURL;
    }
}
