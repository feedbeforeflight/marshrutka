package com.feedbeforeflight.marshrutka.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "Points")
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
    @Getter @Setter
    private boolean active;

    @Column(name = "push_url")
    @Getter @Setter
    private String pushURL;

    @Column(name = "push_enabled", nullable = false)
    @Getter @Setter
    private boolean pushEnabled;

    @Column(name = "push_protocol", nullable = false)
    @Enumerated(EnumType.ORDINAL)
    @Getter @Setter
    private PointEntityProtocol pushProtocol;

    public PointEntity(int id, String name, boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public PointEntity() {

    }

}
