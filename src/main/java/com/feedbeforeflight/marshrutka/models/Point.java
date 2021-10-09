package com.feedbeforeflight.marshrutka.models;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table(name = "Points")
public class Point {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

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

}
