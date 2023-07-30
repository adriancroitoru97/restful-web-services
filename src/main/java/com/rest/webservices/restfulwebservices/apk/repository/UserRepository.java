package com.rest.webservices.restfulwebservices.apk.repository;

import com.rest.webservices.restfulwebservices.apk.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByName(String name);
}
