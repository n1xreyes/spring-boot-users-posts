package com.angelo.demo.repository;

import com.angelo.demo.entity.Post;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByUserId(Long userId);
}

