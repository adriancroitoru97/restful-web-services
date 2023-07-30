package com.rest.webservices.restfulwebservices.apk;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rest.webservices.restfulwebservices.apk.controller.UserController;
import com.rest.webservices.restfulwebservices.apk.entity.Post;
import com.rest.webservices.restfulwebservices.apk.entity.User;
import com.rest.webservices.restfulwebservices.apk.repository.PostRepository;
import com.rest.webservices.restfulwebservices.apk.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PostRepository postRepository;


    private static final User USER_1 = new User(1,"Adrian", LocalDate.now().minusYears(1));
    private static final User USER_2 = new User(2,"Adi", LocalDate.now().minusYears(2));
    private static final User USER_3 = new User(3,"Adita", LocalDate.now().minusYears(3));

    private static final User USER_INVALID = new User(4,"A", LocalDate.now().plusYears(3));

    private static final User USER_31 = new User(3,"AdrianC", LocalDate.now().minusYears(3));
    private static final User USER_32 = new User(5,"Adi", LocalDate.now().minusYears(3));

    private static final Post POST_1 = new Post(1, "My test  post!");
    private static final Post POST_2 = new Post(2, "My test  post2!");

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void retrieveAllUsers() throws Exception {
        List<User> users = Arrays.asList(USER_1, USER_2, USER_3);

        given(userRepository.findAll()).willReturn(users);

        ResultActions response = mvc
                .perform(get("/jpa/users")
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(3)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void retrieveUser() throws Exception {
        Integer id = USER_1.getId();

        given(userRepository.findById(id)).willReturn(Optional.of(USER_1));

        /* Basic case */
        ResultActions response = mvc
                .perform(get("/jpa/users/" + id)
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Adrian"))
                .andExpect(jsonPath("$.birthDate").value("2022-07-13"))
                .andDo(MockMvcResultHandlers.print());

        /* User not found */
        response = mvc
                .perform(get("/jpa/users/" + 342432)
                .with(user("username").password("password")));

        response.andExpect(status().isNotFound())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void createUser() throws Exception {
        when(userRepository.save(any())).thenReturn(USER_1);

        /* Basic working */
        ResultActions response = mvc
                .perform(post("/jpa/users")
                .with(user("username").password("password"))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(USER_1)));

        response.andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        /* Invalid arguments */
        response = mvc
                .perform(post("/jpa/users")
                .with(user("username").password("password"))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(USER_INVALID)));

        response.andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Total errors: 2")))
                .andDo(MockMvcResultHandlers.print());

        /* Duplicate name */
        given(userRepository.findByName(USER_32.getName())).willReturn(Optional.of(USER_32));

        response = mvc
                .perform(post("/jpa/users")
                .with(user("username").password("password"))
                .contentType(APPLICATION_JSON)
                .content(asJsonString(USER_32)));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("already present in database!")))
                .andDo(MockMvcResultHandlers.print());

        /* Duplicate id */
        given(userRepository.findById(USER_31.getId())).willReturn(Optional.of(USER_31));

        response = mvc
                .perform(post("/jpa/users")
                        .with(user("username").password("password"))
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(USER_31)));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("already present in database!")))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void createPostForUser() throws Exception {
        when(postRepository.save(any())).thenReturn(POST_1);
        given(userRepository.findById(USER_INVALID.getId())).willReturn(Optional.of(USER_INVALID));


        /* Basic working */
        given(userRepository.findById(USER_1.getId())).willReturn(Optional.of(USER_1));
        ResultActions response = mvc
                .perform(post("/jpa/users/" + USER_1.getId() + "/posts")
                        .with(user("username").password("password"))
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(POST_1)));

        response.andExpect(status().isCreated())
                .andDo(MockMvcResultHandlers.print());

        /* Invalid user */
        response = mvc
                .perform(post("/jpa/users/" + 3523252 + "/posts")
                        .with(user("username").password("password"))
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(POST_1)));

        response.andExpect(status().isNotFound())
                .andExpect(content().string(containsString(" not found!")))
                .andDo(MockMvcResultHandlers.print());

        /* Duplicate post */
        given(postRepository.findByDescription(POST_1.getDescription())).willReturn(Optional.of(POST_1));
        response = mvc
                .perform(post("/jpa/users/" + USER_1.getId() + "/posts")
                        .with(user("username").password("password"))
                        .contentType(APPLICATION_JSON)
                        .content(asJsonString(POST_1)));

        response.andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("already present in database!")))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void retrievePostsForUser() throws Exception {
        List<Post> posts = Arrays.asList(POST_1, POST_2);
        POST_1.setUser(USER_1);
        POST_2.setUser(USER_1);
        USER_1.setPosts(posts);

        given(userRepository.findById(USER_1.getId())).willReturn(Optional.of(USER_1));
        when(postRepository.save(any())).thenReturn(POST_1);

        ResultActions response = mvc
                .perform(get("/jpa/users/" + USER_1.getId() + "/posts")
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deletePost() throws Exception {
        List<Post> posts = Arrays.asList(POST_1, POST_2);
        POST_1.setUser(USER_1);
        POST_2.setUser(USER_1);
        USER_1.setPosts(posts);

        when(userRepository.findById(USER_1.getId())).thenReturn(Optional.of(USER_1));

        ResultActions response = mvc
                .perform(delete("/jpa/users/1/posts/2")
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        response = mvc
                .perform(get("/jpa/users/1")
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void deleteUser() throws Exception {
        List<Post> posts = Arrays.asList(POST_1, POST_2);
        POST_1.setUser(USER_1);
        POST_2.setUser(USER_1);
        USER_1.setPosts(posts);

        when(userRepository.findById(USER_1.getId())).thenReturn(Optional.of(USER_1));

        ResultActions response = mvc
                .perform(delete("/jpa/users/1/posts/2")
                .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andDo(MockMvcResultHandlers.print());

        response = mvc
                .perform(get("/jpa/users/1")
                        .with(user("username").password("password")));

        response.andExpect(status().isOk())
                .andExpect(content().contentType("application/hal+json"))
                .andDo(MockMvcResultHandlers.print());
    }
}
