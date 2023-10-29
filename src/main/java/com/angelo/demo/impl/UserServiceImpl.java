package com.angelo.demo.impl;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.entity.Post;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.entity.User;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.repository.UserRepository;
import com.angelo.demo.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private RestTemplateClient restTemplate;

    @Value("${posts.api.url}")
    private String postsApi;

    @Value("${users.api.url}")
    private String usersApi;

    @Override
    @Transactional
    public List<User> getAllUsers() throws Exception {
        List<User> usersList = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .toList();

        if (!usersList.isEmpty()) {
            for (User user : usersList) {
                List<Post> postsList = postRepository.findByUserId(user.getId());
                if (!postsList.isEmpty()) {
                    user.setPosts(postsList);
                }
            }
            return usersList;
        }

        return Collections.emptyList();
    }

    @Override
    @Transactional
    public Optional<User> getUserById(Long id) throws Exception {
        return userRepository.findById(id);
    }

    @Override
    @Transactional
    public User addUser(User user) throws Exception {
        try {
            return userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw UserAlreadyExistsException.createWith(user);
        }
    }

    @Override
    @Transactional
    public User changeUser(User user) throws Exception {
        LOGGER.info("updating user with ID {}", user.getId());
        if (userRepository.existsById(user.getId())) {
            return userRepository.save(user);
        }
        throw UserNotFoundException.createWith(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) throws Exception {
        LOGGER.info("deleting user with ID {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
    }

    @Override
    @Transactional
    public void fetchAllUsersFromApi() throws Exception {
        LOGGER.info("fetch all posts from API");
        ResponseEntity<Post[]> postResponse = restTemplate.getForEntity(postsApi, Post[].class);
        List<Post> posts = postResponse.hasBody() ? Arrays.asList(Objects.requireNonNull(postResponse.getBody(), "")) : Collections.emptyList();
        if (!posts.isEmpty()) {
            postRepository.saveAll(Objects.requireNonNull(posts));
        }

        LOGGER.info("get all users from API");
        ResponseEntity<User[]> userResponse = restTemplate.getForEntity(usersApi, User[].class);
        if (userResponse.hasBody() && Objects.requireNonNull(userResponse.getBody()).length > 0) {
            User[] users = userResponse.getBody();

            setPostsToUsers(posts, users);

            userRepository.saveAll(Arrays.asList(users));
        }
    }

    private void setPostsToUsers(List<Post> posts, User[] users) {
        if (!posts.isEmpty()) {
            Map<Long, List<Post>> postsByUserId = posts.stream()
                    .filter(post -> post.getUserId() != null)
                    .collect(Collectors.groupingBy(Post::getUserId));

            for (User user : users) {
                LOGGER.debug("UserID {} data: {}", user.getId(), user);
                List<Post> userPosts = postsByUserId.get(user.getId());
                if (null != userPosts) {
                    if (null != user.getPosts()) {
                        user.getPosts().addAll(userPosts);
                    } else {
                        user.setPosts(userPosts);
                    }
                    LOGGER.debug("User with ID {} : {}", user.getId(), user);
                }
            }
        }
    }
}
