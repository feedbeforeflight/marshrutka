package com.feedbeforeflight.marshrutka.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Points")
@AllArgsConstructor
public class PointEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    @Getter @Setter
    private int id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    @Getter @Setter
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "receive_url")
    @Getter @Setter
    private String receiveURL;

    public PointEntity() {

    }

    public boolean isActive() { return active; }

    public void setActive(boolean active) { this.active = active; }
}
