package com.feedbeforeflight.marshrutka.dao.management;

import com.feedbeforeflight.marshrutka.models.management.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

}
