package com.angelo.demo.post;

import com.angelo.demo.post.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends CrudRepository<Post, Long> {
    List<Post> findByUserId(Long userId);

    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.userId = :userId")
    Post findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}

