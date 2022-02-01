package com.feedbeforeflight.marshrutka.models.management;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "UserRoles")
public class UserRole implements GrantedAuthority {

    @Id
    @GeneratedValue
    @Column(name="id", nullable = false)
    @Getter
    private long id;

    @Column(name = "name", length = 50, nullable = false, unique = true)
    @Getter @Setter
    private String name;

    @Transient
    @ManyToMany(mappedBy = "roles")
    @Getter @Setter
    private Set<User> users;

    @Override
    public String getAuthority() {
        return getName();
    }

    public UserRole(long id, String name) {
        this.id = id;
        this.name = name;
    }
}
