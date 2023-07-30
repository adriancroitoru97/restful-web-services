package com.rest.webservices.restfulwebservices.apk.repository;

import com.rest.webservices.restfulwebservices.apk.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Optional<Post> findByDescription(String name);
}
