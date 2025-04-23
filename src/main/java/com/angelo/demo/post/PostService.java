package com.angelo.demo.post;

import com.angelo.demo.post.dto.PostDto;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
@Slf4j
public class PostService {

    private static final String POSTS_PATH = "/posts";
    
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebClient webClient;

    @Autowired
    private Mapper mapper;

    /**
     * Finds all posts from the database
     * @return List of PostDto
     */
    @Transactional
    public List<PostDto> findAll() {
        log.info("fetch all posts from database");

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
        log.info("fetching individual post by ID");

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
        log.info("retrieving all posts by User ID {}", userId);
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
        log.info("adding post --- checking if user exists");
        if (!userRepository.existsById(userId)) {
            log.error("User does not exist");
            String message = MessageFormat.format("User with id {0} does not exist", userId);
            throw new PostInvalidException(message);
        }

        Post post = mapper.dtoToPost(userId, dto);

        try {
            return mapper.postToDto(postRepository.save(post));
        } catch (Exception e) {
            log.error("An exception occurred when attempting to save post");
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
        log.info("updating post --- checking if user exists");
        if (!userRepository.existsById(userId)) {
            log.error("User does not exist");
            String message = MessageFormat.format("User with id {0} does not exist", userId);
            throw new PostInvalidException(message);
        }

        if(!postRepository.existsById(id)) {
            log.error("Post ID does not exist");
            String message = MessageFormat.format("Post with id {0} does not exist", id);
            throw new PostNotFoundException(message);
        }

        if (null == postRepository.findByIdAndUserId(id, userId)) {
            log.error("This post does not belong to the provide userID");
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
                log.info("post updated for post id {} and userID {}", id, userId);
                return mapper.postToDto(savedPost);
            } else {
                throw new PostNotFoundException("Post to update not found");
            }
        } catch (Exception e) {
            log.error("An exception occurred when attempting to save post");
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
        log.info("Deleting post with ID {}", id);
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
        log.info("fetching users and posts from path: {}", POSTS_PATH);
        try {
            ResponseEntity<List<Post>> postsResponseEntity = webClient.get()
                    .uri(POSTS_PATH)
                    .retrieve()
                    .toEntityList(Post.class)
                    .block(); // Block for synchronous execution

            if (postsResponseEntity != null && postsResponseEntity.hasBody()) {
                List<Post> posts = postsResponseEntity.getBody();
                if (posts != null && !posts.isEmpty()) {
                    log.info("Successfully fetched {} posts from API. Saving to database...", posts.size());
                    postRepository.saveAll(posts);
                    log.info("Posts saved successfully.");
                } else {
                    log.info("Fetched posts from API, but the list was null or empty. No posts saved.");
                }
            } else {
                log.warn("Received null or empty response entity when fetching posts.");
            }
        } catch (WebClientResponseException e) {
            log.error("Error fetching posts from API: Status code {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new Exception(MessageFormat.format("Error fetching posts from API: {0}", e.getMessage()), e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during fetch or save of posts: {}", e.getMessage(), e);
            throw new Exception("An unexpected error occurred while fetching and saving posts.", e);
        }
    }
}
