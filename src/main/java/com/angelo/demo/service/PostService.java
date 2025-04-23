package com.angelo.demo.service;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.dto.PostDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplateClient restTemplate;

    @Autowired
    private WebClient webClient;

    @Autowired
    private Mapper mapper;

    @Value("${posts.api.url}")
    private String postsEndpoint;


    /**
     * Finds all posts from the database
     * @return List of PostDto
     */
    @Transactional
    public List<PostDto> findAll() {
        LOGGER.info("fetch all posts from database");

        List<Post> postList = StreamSupport.stream(postRepository.findAll().spliterator(), false)
                .toList();

        List<PostDto> postDtos = postList.stream().map(post -> mapper.postToDto(post)).toList();

        return postDtos.isEmpty() ? Collections.emptyList() : postDtos;
    }

    /**
     * Finds an individual post by post ID
     * @param id
     * @return PostDto
     */
    @Transactional
    public PostDto findById(Long id) {
        LOGGER.info("fetching individual post by ID");

        Optional<Post> post = postRepository.findById(id);

        return post.map(value -> mapper.postToDto(value)).orElse(null);
    }

    /**
     * Finds all posts by userID.
     * @param userId
     * @return List of PostDto containing all posts for the given userId
     */
    @Transactional
    public List<PostDto> findAllPostsByUserId(Long userId) {
        LOGGER.info("retrieving all posts by User ID {}", userId);
        List<Post> postList = postRepository.findByUserId(userId);

        List<PostDto> postDtos = postList.stream().map(post -> mapper.postToDto(post)).toList();

        return postDtos.isEmpty() ? Collections.emptyList() : postDtos;
    }

    /**
     * Saves new post. Checks if the user exists first
     * @param userId
     * @param dto
     * @return
     */
    @Transactional
    public PostDto savePost(Long userId, PostDto dto) {
        LOGGER.info("adding post --- checking if user exists");
        if (!userRepository.existsById(userId)) {
            LOGGER.error("User does not exist");
            String message = MessageFormat.format("User with id {0} does not exist", userId);
            throw new PostInvalidException(message);
        }

        Post post = mapper.dtoToPost(userId, dto);

        try {
            return mapper.postToDto(postRepository.save(post));
        } catch (Exception e) {
            LOGGER.error("An exception occurred when attempting to save post");
            String message = MessageFormat.format("An exception occurred: {0} - cause {1}", e.getMessage(), e.getCause());
            throw new PostInvalidException(message);
        }
    }

    /**
     * Updates an existing post. Checks if the user exists, then checks if post to be updated exists,
     * then ensures the post ID belongs to the given user
     * @param id
     * @param userId
     * @param dto
     * @return
     */
    @Transactional
    public PostDto updatePost(Long id, Long userId, PostDto dto) {
        LOGGER.info("updating post --- checking if user exists");
        if (!userRepository.existsById(userId)) {
            LOGGER.error("User does not exist");
            String message = MessageFormat.format("User with id {0} does not exist", userId);
            throw new PostInvalidException(message);
        }

        if(!postRepository.existsById(id)) {
            LOGGER.error("Post ID does not exist");
            String message = MessageFormat.format("Post with id {0} does not exist", id);
            throw new PostNotFoundException(message);
        }

        if (null == postRepository.findByIdAndUserId(id, userId)) {
            LOGGER.error("This post does not belong to the provide userID");
            String message = MessageFormat.format("Post with id {0} does not belong to userID {1}", id, userId);
            throw new PostInvalidException(message);
        }

        try {
            Optional<Post> post = postRepository.findById(id);
            if(post.isPresent()) {
                Post postToUpdate = post.get();
                postToUpdate.setTitle(dto.getTitle());
                postToUpdate.setBody(dto.getBody());

                Post savedPost = postRepository.save(postToUpdate);
                LOGGER.info("post updated for post id {} and userID {}", id, userId);
                return mapper.postToDto(savedPost);
            } else {
                throw new PostNotFoundException("Post to update not found");
            }
        } catch (Exception e) {
            LOGGER.error("An exception occurred when attempting to save post");
            String message = MessageFormat.format("An exception occurred: {0} - cause {1}", e.getMessage(), e.getCause());
            throw new PostInvalidException(message);
        }
    }

    /**
     * Delete an individual post by ID. Checks if the post to be deleted exists
     * @param id
     * @throws Exception
     */
    @Transactional
    public void deleteById(Long id) throws Exception {
        LOGGER.info("Deleting post with ID {}", id);
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new PostNotFoundException("Post not found");
        }
    }

    /**
     * Makes an API call to fetch posts and save to the database.
     * @throws Exception
     */
    @Transactional
    public void fetchAndSavePosts() throws Exception {
        ResponseEntity<List<Post>> posts = webClient.get()
                .uri("/posts")
                .retrieve()
                .toEntityList(Post.class)
                .block();

        postRepository.saveAll(Objects.requireNonNull(Objects.requireNonNull(posts).getBody()));
//        try {
//            ResponseEntity<Post[]> response = restTemplate.getForEntity(postsEndpoint, Post[].class);
//            postRepository.saveAll(Arrays.asList(Objects.requireNonNull(response.getBody())));
//        } catch (NullPointerException e) {
//            throw new Exception(e.getMessage());
//        } catch (Exception e) {
//            throw new Exception(e.getMessage());
//        }

    }
}
