package com.angelo.demo.controller;

import com.angelo.demo.dto.PostDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.service.PostService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/posts")
@AllArgsConstructor
public class PostController {
    private static final Logger LOGGER = LoggerFactory.getLogger(PostController.class);

    @Autowired
    PostService postService;


    @GetMapping(produces = "application/json")
    public List<PostDto> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public ResponseEntity<Object> findPost(@PathVariable Long id) {
        try {
            PostDto postDto = postService.findById(id);
            if (null != postDto) {
                return new ResponseEntity<>(postDto, HttpStatus.OK);
            }
        } catch (PostNotFoundException e) {
            LOGGER.error("Post not found");
            throw e;
        }
        return new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND);
    }

    @PostMapping(produces = "application/json")
    public ResponseEntity<List<PostDto>> getAllUserPosts(@RequestParam Long userId) {
        try {
            return new ResponseEntity<>(postService.findAllPostsByUserId(userId), HttpStatus.OK);
        } catch (PostNotFoundException e) {
            LOGGER.error("Posts not found");
            throw e;
        }
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PostDto> addPost(@RequestParam Long userId, @RequestBody PostDto dto) {

        try {
            return new ResponseEntity<>(postService.savePost(userId, dto), HttpStatus.CREATED);
        } catch (PostInvalidException e) {
            LOGGER.error("Post invalid. Ensure title and body are not null or empty");
            throw e;
        }

    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<PostDto> updatePost(@RequestParam Long id, @RequestParam Long userId, @RequestBody PostDto dto) {

        try {
            return new ResponseEntity<>(postService.updatePost(id, userId, dto), HttpStatus.OK);
        } catch (PostInvalidException e) {
            LOGGER.error("Post invalid. Ensure title and body are not null or empty");
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable Long id) throws Exception {
        postService.deleteById(id);
        return new ResponseEntity<>("Post deleted", HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchPosts() throws Exception {
        postService.fetchPosts();
        return new ResponseEntity<>("Posts retrieved from JSONPlaceholder API", HttpStatus.OK);
    }
}
