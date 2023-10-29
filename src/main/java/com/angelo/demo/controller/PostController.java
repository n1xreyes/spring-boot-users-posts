package com.angelo.demo.controller;

import com.angelo.demo.entity.Post;
import com.angelo.demo.service.PostService;
import lombok.AllArgsConstructor;
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

    @Autowired
    PostService postService;


    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findPost(@PathVariable Long id) {
        Optional<Post> post = postService.findById(id);
        return post.<ResponseEntity<Object>>map(res ->
                new ResponseEntity<>(res, HttpStatus.OK)).orElseGet(() ->
                new ResponseEntity<>("Post not found", HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody Post post) {
        return new ResponseEntity<>(postService.save(post), HttpStatus.CREATED);
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
