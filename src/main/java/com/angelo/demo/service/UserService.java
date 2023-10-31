package com.angelo.demo.service;

import com.angelo.demo.dto.UserAndPostsDto;

import java.util.List;

public interface UserService {
    public List<UserAndPostsDto> getAllUsers() throws Exception;
    public UserAndPostsDto getUserById(Long id) throws Exception;
    public UserAndPostsDto addUser(UserAndPostsDto dto) throws Exception;
    public UserAndPostsDto changeUser(UserAndPostsDto dto) throws Exception;
    public void deleteUser(Long id) throws Exception;
    public void fetchAllUsersFromApi() throws Exception;

}
