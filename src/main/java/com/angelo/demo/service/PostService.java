package com.angelo.demo.service;

import com.angelo.demo.dto.PostDto;
import com.angelo.demo.entity.Post;

import java.util.List;
import java.util.Optional;

public interface PostService {

    public List<PostDto> findAll();

    public PostDto findById(Long id);

    public List<PostDto> findAllPostsByUserId(Long userId);

    public PostDto savePost(Long userId, PostDto dto);

    public PostDto updatePost(Long id, Long userId, PostDto dto);

    public void deleteById(Long id) throws Exception;

    public void fetchPosts() throws Exception;
}
