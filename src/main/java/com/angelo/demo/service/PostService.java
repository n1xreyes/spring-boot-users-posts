package com.angelo.demo.service;

import com.angelo.demo.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {

    public List<Post> findAll();

    public Optional<Post> findById(Long id);

    public Post save(Post post);

    public void deleteById(Long id) throws Exception;

    public void fetchPosts() throws Exception;
}
