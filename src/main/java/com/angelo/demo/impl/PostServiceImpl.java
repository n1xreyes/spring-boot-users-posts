package com.angelo.demo.impl;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.dto.PostDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.repository.UserRepository;
import com.angelo.demo.service.PostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class PostServiceImpl implements PostService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostServiceImpl.class);
    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplateClient restTemplate;

    @Autowired
    private Mapper mapper;

    @Value("${posts.api.url}")
    private String api;


    @Override
    @Transactional
    public List<PostDto> findAll() {
        LOGGER.info("fetch all posts from database");

        List<Post> postList = StreamSupport.stream(postRepository.findAll().spliterator(), false)
                .toList();

        List<PostDto> postDtos = postList.stream().map(post -> mapper.postToDto(post)).toList();

        return postDtos.isEmpty() ? Collections.emptyList() : postDtos;
    }

    @Override
    @Transactional
    public PostDto findById(Long id) {
        LOGGER.info("fetching individual post by ID");

        Optional<Post> post = postRepository.findById(id);

        return post.map(value -> mapper.postToDto(value)).orElse(null);
    }

    @Override
    @Transactional
    public List<PostDto> findAllPostsByUserId(Long userId) {
        LOGGER.info("retrieving all posts by User ID {}", userId);
        List<Post> postList = postRepository.findByUserId(userId);

        List<PostDto> postDtos = postList.stream().map(post -> mapper.postToDto(post)).toList();

        return postDtos.isEmpty() ? Collections.emptyList() : postDtos;
    }

    @Override
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

    @Override
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

    @Override
    @Transactional
    public void deleteById(Long id) throws Exception {
        LOGGER.info("Deleting post with ID {}", id);
        if (postRepository.existsById(id)) {
            postRepository.deleteById(id);
        } else {
            throw new PostNotFoundException("Post not found");
        }
    }

    @Override
    @Transactional
    public void fetchPosts() throws Exception {
        try {
            ResponseEntity<Post[]> response = restTemplate.getForEntity(api, Post[].class);
            postRepository.saveAll(Arrays.asList(Objects.requireNonNull(response.getBody())));
        } catch (NullPointerException e) {
            throw new Exception(e.getMessage());
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        }

    }

}
