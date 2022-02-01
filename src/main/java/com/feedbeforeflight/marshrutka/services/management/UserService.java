package com.feedbeforeflight.marshrutka.services.management;

import com.feedbeforeflight.marshrutka.dao.management.UserRepository;
import com.feedbeforeflight.marshrutka.dao.management.UserRoleRepository;
import com.feedbeforeflight.marshrutka.models.management.User;
import com.feedbeforeflight.marshrutka.models.management.UserRole;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository userRepository, UserRoleRepository userRoleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username);

        if (user == null) {
            throw new UsernameNotFoundException("Username " + username + " not found");
        }

        return user;
    }

    public User findUserByID(Long userId) {
        Optional<User> persistedUser = userRepository.findById(userId);
        return persistedUser.orElse(new User());
    }

    public List<User> allUsers() {
        return userRepository.findAll();
    }

    public boolean saveUser(User user) {
        User persistedUser = userRepository.findByUsername(user.getUsername());

        if (persistedUser != null) {
            return false;
        }

        user.setRoles(Collections.singleton(new UserRole(1L, "ROLE_USER")));
        userRepository.save(user);

        return true;
    }

    public  boolean deleteUser(Long userId) {
        if (userRepository.findById(userId).isPresent()) {
            userRepository.deleteById(userId);
            return true;
        }

        return false;
    }

}
