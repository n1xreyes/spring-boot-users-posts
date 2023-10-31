package com.angelo.demo.mapper;

import com.angelo.demo.dto.UserAndPostsDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.entity.User;
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

    public Post dtoToPost(Post dto) {
        return modelMapper.map(dto, Post.class);
    }
}
