package com.angelo.demo.impl;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.dto.UserAndPostsDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.entity.User;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.repository.UserRepository;
import com.angelo.demo.service.UserService;
import com.angelo.demo.util.EmailValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
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

    @Autowired
    private Mapper mapper;

    @Value("${posts.api.url}")
    private String postsApi;

    @Value("${users.api.url}")
    private String usersApi;

    /**
     * Gets all users from the database. This makes a database call to userRepository to build a list of users
     * then for each user, makes a database call to postRepository to retrieve posts by user's ID.
     * @return List of UserAndPostsDto or an empty list
     * @throws Exception
     */
    @Override
    @Transactional
    public List<UserAndPostsDto> getAllUsers() throws Exception {
        LOGGER.info("Fetch users from database");

        List<User> usersList = StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .toList();

        List<UserAndPostsDto> userAndPostsDtos = new ArrayList<>();

        if (!usersList.isEmpty()) {
            for (User user : usersList) {
                UserAndPostsDto dto = mapper.toDto(user);
                List<Post> postsList = postRepository.findByUserId(user.getId());
                if (!postsList.isEmpty() && null != dto) {
                    dto.setPosts(postsList);
                }
                userAndPostsDtos.add(dto);
            }
            return userAndPostsDtos;
        }
        LOGGER.warn("No users found");

        return Collections.emptyList();
    }

    /**
     * retrieves an individual  user and posts by userId
     * @param id
     * @return UserAndPostsDto or null
     * @throws Exception
     */
    @Override
    @Transactional
    public UserAndPostsDto getUserById(Long id) throws Exception {
        LOGGER.info("fetching individual user by ID");

        User user = userRepository.findById(id).isPresent() ? userRepository.findById(id).get()
                : null;

        if (null != user) {
            LOGGER.info("User fetched from database. Fetching posts by this user from the database");
            LOGGER.debug("User with id {} fetched from database", user.getId());
            List<Post> posts = postRepository.findByUserId(id);
            return mapper.toDto(user, posts);
        }

        return null;
    }

    /**
     * Adds a new user and posts (if any) to the database. This first validates the properties
     * to check for missing or invalid values
     * @param @UserAndPosts dto
     * @return UserAndPostsDto
     * @throws Exception
     */
    @Override
    @Transactional
    public UserAndPostsDto addUser(UserAndPostsDto dto) throws Exception {
        validateUser(dto);
        validateNewUser(dto);

        LOGGER.info("adding new user");
        User user = mapper.dtoToUser(dto);
        List<Post> posts = dto.getPosts();

        try {
            User savedUser = userRepository.save(user);
            if (null != posts && !posts.isEmpty()) {
                // assign the userID to each post
                LOGGER.info("Assigning each post the userID");
                LOGGER.debug("new User ID {}", savedUser.getId());
                posts.forEach(post -> post.setUserId(savedUser.getId()));
                LOGGER.info("adding posts to new user");
                postRepository.saveAll(posts);
            }
            return mapper.toDto(savedUser, posts);
        } catch (DataIntegrityViolationException e) {
            String errorMessage = MessageFormat.format("An exception occurred {0} - cause: {1}", e.getMessage(), e.getCause());
            throw new UserAlreadyExistsException(errorMessage);
        }

    }

    /**
     * Updates an existing user
     * @param @UserAndPosts dto
     * @return UserAndPostsDto
     * @throws Exception
     */
    @Override
    @Transactional
    public UserAndPostsDto changeUser(UserAndPostsDto dto) throws Exception {
        validateUser(dto);

        LOGGER.info("updating user with ID {}", dto.getId());
        if (userRepository.existsById(dto.getId())) {
            try {
                User savedUser = userRepository.save(mapper.dtoToUser(dto));
                List<Post> posts = postRepository.findByUserId(savedUser.getId());
                LOGGER.info("user updated");
                return mapper.toDto(savedUser, posts);
            } catch (Exception e) {
                String errorMessage = MessageFormat.format("An exception occurred {0} - cause: {1}", e.getMessage(), e.getCause());
                throw new Exception(errorMessage);
            }
        }
        LOGGER.error("Unable to update user with ID {}", dto.getId());
        throw new UserNotFoundException("User to update not found");
    }

    /**
     * Deletes an existing user by id. First checks if it exists in the database.
     * @param id
     * @throws Exception
     */
    @Override
    @Transactional
    public void deleteUser(Long id) throws Exception {
        LOGGER.info("deleting user with ID {}", id);
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        } else {
            throw new UserNotFoundException("User not found");
        }
    }

    /**
     * Makes 2 API calls to retrieve users and posts, then saves them to the database
     * @throws Exception
     */
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

            LOGGER.debug("users and posts: {}", (Object) users);

            userRepository.saveAll(Arrays.asList(users));
        }
    }

    /**
     * validates new user. checks if username and email are unique
     * @param dto
     */
    private void validateNewUser(UserAndPostsDto dto) {
        LOGGER.info("validate new user");

        if (userRepository.existsByUsername(dto.getUserName())) {
            LOGGER.error("User exists with same username");
            String errorMessage = MessageFormat.format("User with userName {0} exists", dto.getUserName());
            throw new UserAlreadyExistsException(errorMessage);
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            LOGGER.error("User exists with same email");
            String errorMessage = MessageFormat.format("User with email {0} exists", dto.getEmail());
            throw new UserAlreadyExistsException(errorMessage);
        }
    }

    /**
     * validates user. Checks if full name, username and email are missing.
     * @param dto
     */
    private void validateUser(UserAndPostsDto dto) {
        LOGGER.info("validate existing user");

        if (null == dto.getFullName() || dto.getFullName().isEmpty()) {
            throw new UserInvalidException("Full name required");
        }

        if (null == dto.getUserName() || dto.getUserName().isEmpty()) {
            throw new UserInvalidException("Username required");
        }

        if (null == dto.getEmail() || dto.getEmail().isEmpty()) {
            throw new UserInvalidException("Email required");
        }

        if (!EmailValidator.validateEmail(dto.getEmail())) {
            throw new UserInvalidException("Email format is invalid. Ensure email is in a correct format");
        }
    }
}
