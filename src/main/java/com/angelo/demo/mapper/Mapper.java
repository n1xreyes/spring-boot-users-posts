package com.angelo.demo.mapper;

import com.angelo.demo.post.dto.PostDto;
import com.angelo.demo.common.dto.UserAndPostsDto;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.user.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Mapper {
    private final ModelMapper modelMapper;

    public Mapper() {
        this.modelMapper = new ModelMapper();
        configureMappings();
    }

    private void configureMappings() {
        modelMapper.createTypeMap(User.class, UserAndPostsDto.class)
                .addMapping(User::getUsername, UserAndPostsDto::setUserName)
                .addMapping(User::getName, UserAndPostsDto::setFullName);

        modelMapper.getConfiguration().setAmbiguityIgnored(true);
    }

    public UserAndPostsDto toDto(User user, List<Post> posts) {
        UserAndPostsDto dto = modelMapper.map(user, UserAndPostsDto.class);
        dto.setPosts(posts);
        return dto;
    }

    public UserAndPostsDto toDto(User user) {
        return modelMapper.map(user, UserAndPostsDto.class);
    }

    public User dtoToUser(UserAndPostsDto dto) {
        User user = modelMapper.map(dto, User.class);
        user.setName(dto.getFullName());
        return user;
    }

    public Post dtoToPost(Long userId, PostDto dto) {
        Post post = modelMapper.map(dto, Post.class);
        post.setUserId(userId);

        return post;
    }

    public PostDto postToDto(Post post) { return modelMapper.map(post, PostDto.class); }
}
