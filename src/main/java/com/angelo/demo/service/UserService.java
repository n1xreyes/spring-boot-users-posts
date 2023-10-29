package com.angelo.demo.service;

import com.angelo.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public List<User> getAllUsers() throws Exception;
    public Optional<User> getUserById(Long id) throws Exception;
    public User addUser(User user) throws Exception;
    public User changeUser(User user) throws Exception;
    public void deleteUser(Long id) throws Exception;
    public void fetchAllUsersFromApi() throws Exception;

}
