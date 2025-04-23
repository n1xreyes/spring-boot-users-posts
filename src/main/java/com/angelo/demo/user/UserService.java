package com.angelo.demo.user;

import com.angelo.demo.common.dto.UserAndPostsDto;
import com.angelo.demo.user.entity.User;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.post.PostRepository;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.util.EmailValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.StreamSupport;

@Slf4j
@Service
public class UserService {

    private static final String POSTS_PATH = "/posts";
    private static final String USERS_PATH = "/users";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private Mapper mapper;

    /**
     * Gets all users from the database. This makes a database call to userRepository to build a list of users
     * then for each user, makes a database call to postRepository to retrieve posts by user's ID.
     * @return List of UserAndPostsDto or an empty list
     * @throws Exception
     */
    @Transactional
    public List<UserAndPostsDto> getAllUsers() throws Exception {
        log.info("Fetch users from database");

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
        log.warn("No users found");

        return Collections.emptyList();
    }

    /**
     * retrieves an individual  user and posts by userId
     * @param id
     * @return UserAndPostsDto or null
     * @throws Exception
     */
    @Transactional
    public UserAndPostsDto getUserById(Long id) throws Exception {
        log.info("fetching individual user by ID");

        User user = userRepository.findById(id).isPresent() ? userRepository.findById(id).get()
                : null;

        if (null != user) {
            log.info("User fetched from database. Fetching posts by this user from the database");
            log.debug("User with id {} fetched from database", user.getId());
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
    @Transactional
    public UserAndPostsDto addUser(UserAndPostsDto dto) throws Exception {
        validateUser(dto);
        validateNewUser(dto);

        log.info("adding new user");
        User user = mapper.dtoToUser(dto);
        List<Post> posts = dto.getPosts();

        try {
            User savedUser = userRepository.save(user);
            if (null != posts && !posts.isEmpty()) {
                // assign the userID to each post
                log.info("Assigning each post the userID");
                log.debug("new User ID {}", savedUser.getId());
                posts.forEach(post -> post.setUserId(savedUser.getId()));
                log.info("adding posts to new user");
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
    @Transactional
    public UserAndPostsDto changeUser(UserAndPostsDto dto) throws Exception {
        validateUser(dto);

        log.info("updating user with ID {}", dto.getId());
        if (userRepository.existsById(dto.getId())) {
            try {
                User savedUser = userRepository.save(mapper.dtoToUser(dto));
                List<Post> posts = postRepository.findByUserId(savedUser.getId());
                log.info("user updated");
                return mapper.toDto(savedUser, posts);
            } catch (Exception e) {
                String errorMessage = MessageFormat.format("An exception occurred {0} - cause: {1}", e.getMessage(), e.getCause());
                throw new Exception(errorMessage);
            }
        }
        log.error("Unable to update user with ID {}", dto.getId());
        throw new UserNotFoundException("User to update not found");
    }

    /**
     * Deletes an existing user by id. First checks if it exists in the database.
     * @param id
     * @throws Exception
     */
    @Transactional
    public void deleteUser(Long id) throws Exception {
        log.info("deleting user with ID {}", id);
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
    @Transactional
    public void fetchAllUsersFromApi() throws Exception {
        log.info("Fetch all posts from API using WebClient and path: {}", POSTS_PATH);

        ResponseEntity<List<Post>> postResponseEntity = webClient.get()
                // *** Use relative path constant ***
                .uri(POSTS_PATH)
                .retrieve()
                .toEntityList(Post.class)
                .block();

        List<Post> posts = Collections.emptyList();
        if (postResponseEntity != null && postResponseEntity.hasBody()) {
            posts = postResponseEntity.getBody();
            if (posts != null && !posts.isEmpty()) {
                log.info("Fetched {} posts from API. Saving...", posts.size());
                postRepository.saveAll(posts);
            } else {
                log.info("Fetched posts from API, but the list was null or empty.");
            }
        } else {
            log.warn("Failed to fetch posts or response was empty.");
        }


        log.info("Get all users from API using WebClient and path: {}", USERS_PATH);
        ResponseEntity<List<User>> userResponseEntity = webClient.get()
                .uri(USERS_PATH)
                .retrieve()
                .toEntityList(User.class)
                .block();

        if (userResponseEntity != null && userResponseEntity.hasBody()) {
            List<User> users = userResponseEntity.getBody();
            if (users != null && !users.isEmpty()) {
                log.info("Fetched {} users from API. Saving...", users.size());
                log.debug("Users fetched: {}", users);
                userRepository.saveAll(users);
            } else {
                log.info("Fetched users from API, but the list was null or empty.");
            }
        } else {
            log.warn("Failed to fetch users or response was empty.");
        }
    }

    /**
     * validates new user. checks if username and email are unique
     * @param dto
     */
    private void validateNewUser(UserAndPostsDto dto) {
        log.info("validate new user");

        if (userRepository.existsByUsername(dto.getUserName())) {
            log.error("User exists with same username");
            String errorMessage = MessageFormat.format("User with userName {0} exists", dto.getUserName());
            throw new UserAlreadyExistsException(errorMessage);
        }

        if (userRepository.existsByEmail(dto.getEmail())) {
            log.error("User exists with same email");
            String errorMessage = MessageFormat.format("User with email {0} exists", dto.getEmail());
            throw new UserAlreadyExistsException(errorMessage);
        }
    }

    /**
     * validates user. Checks if full name, username and email are missing.
     * @param dto
     */
    private void validateUser(UserAndPostsDto dto) {
        log.info("validate existing user");

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
