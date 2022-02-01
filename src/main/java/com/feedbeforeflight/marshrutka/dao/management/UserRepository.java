package com.feedbeforeflight.marshrutka.dao.management;

import com.feedbeforeflight.marshrutka.models.management.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByUsername(String username);

}
