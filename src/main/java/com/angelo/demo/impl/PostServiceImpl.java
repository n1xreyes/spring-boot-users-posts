package com.angelo.demo.impl;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.entity.Post;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private RestTemplateClient restTemplate;
    @Value("${posts.api.url}")
    private String api;


    @Override
    @Transactional
    public List<Post> findAll() {
        List<Post> postList = StreamSupport.stream(postRepository.findAll().spliterator(), false)
                .toList();

        return postList.isEmpty() ? Collections.emptyList() : postList;
    }

    @Override
    @Transactional
    public Optional<Post> findById(Long id) { return postRepository.findById(id); }

    @Override
    @Transactional
    public Post save(Post post) {
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public void deleteById(Long id) throws Exception {
        postRepository.deleteById(id);
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
