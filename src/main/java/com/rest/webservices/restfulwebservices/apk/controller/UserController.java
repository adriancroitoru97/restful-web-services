package com.rest.webservices.restfulwebservices.apk.controller;

import com.rest.webservices.restfulwebservices.apk.entity.Post;
import com.rest.webservices.restfulwebservices.apk.entity.User;
import com.rest.webservices.restfulwebservices.apk.exception.AlreadyPresentException;
import com.rest.webservices.restfulwebservices.apk.exception.ResourceNotFoundException;
import com.rest.webservices.restfulwebservices.apk.repository.PostRepository;
import com.rest.webservices.restfulwebservices.apk.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    private final PostRepository postRepository;


    @GetMapping("/jpa/users")
    public List<User> retrieveAllUsers() {
        return userRepository.findAll();
    }

    @GetMapping("/jpa/users/{id}")
    public EntityModel<User> retrieveUser(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("id: " + id);
        }

        EntityModel<User> entityModel = EntityModel.of(user.get());
        WebMvcLinkBuilder link = linkTo(methodOn(this.getClass()).retrieveAllUsers());
        entityModel.add(link.withRel("all-users"));

        return entityModel;
    }

    @DeleteMapping("/jpa/users/{id}")
    public void deleteUser(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User with id `" + id + "` not found!");
        }

        for (Post post : user.get().getPosts()) {
            postRepository.deleteById(post.getId());
        }


        userRepository.deleteById(id);
    }

    @DeleteMapping("/jpa/users/{id}/posts/{postid}")
    public void deletePost(@PathVariable Integer id, @PathVariable Integer postid) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User with id `" + id + "` not found!");
        }

        Optional<Post> toDeletePost = user.get().getPosts()
                .stream()
                .filter(post -> Objects.equals(post.getId(), postid))
                .findFirst();
        if (toDeletePost.isEmpty()) {
            throw new ResourceNotFoundException("Post with id `" + postid + "` not found!");
        }

        postRepository.deleteById(postid);
    }

    @GetMapping("/jpa/users/{id}/posts")
    public List<Post> retrievePostsForUser(@PathVariable Integer id) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User with id `" + id + "` not found!");
        }

        return user.get().getPosts();
    }

    @PostMapping("/jpa/users")
    public ResponseEntity<Object> createUser(@Valid @RequestBody User user) {
        Optional<User> user2 = userRepository.findByName(user.getName());
        if (user2.isPresent()) {
            throw new AlreadyPresentException(
                    "The user with name `" + user.getName() + "' is already present in database!"
            );
        }

        user2 = userRepository.findById(user.getId());
        if (user2.isPresent()) {
            throw new AlreadyPresentException(
                    "The user with id `" + user.getId() + "' is already present in database!"
            );
        }

        User savedUser = userRepository.save(user);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(savedUser.getId())
                        .toUri();

        return ResponseEntity.created(location).build();
    }

    @PostMapping("/jpa/users/{id}/posts")
    public ResponseEntity<Object> createPostForUser(@PathVariable Integer id, @Valid @RequestBody Post post) {
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User with id `" + id + "` not found!");
        }

        Optional<Post> post2 = postRepository.findByDescription(post.getDescription());
        if (post2.isPresent()) {
            throw new AlreadyPresentException(
                    "The post with description `" + post.getDescription() + "' is already present in database!"
            );
        }

        post.setUser(user.get());

        Post savedPost = postRepository.save(post);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedPost.getId())
                .toUri();

        return ResponseEntity.created(location).build();
    }
}
