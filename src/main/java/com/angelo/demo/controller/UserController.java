package com.angelo.demo.controller;

import com.angelo.demo.dto.UserAndPostsDto;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private static final String USER_NOT_FOUND = "User not found";

    @Autowired
    UserService userService;

    @GetMapping(produces = "application/json")
    public ResponseEntity<Object> getAllUsers() throws Exception {
        List<UserAndPostsDto> userAndPostsDtos = userService.getAllUsers();
        if (!userAndPostsDtos.isEmpty()) {
            return new ResponseEntity<>(userAndPostsDtos, HttpStatus.OK);
        }

        return new ResponseEntity<>("Users not found", HttpStatus.NOT_FOUND);
    }

    @GetMapping(value = "/find-by-id", produces = "application/json")
    public ResponseEntity<Object> findUserById(@RequestParam Long id) throws Exception{

        try {
            UserAndPostsDto user = userService.getUserById(id);
            if (null != user) {
                return new ResponseEntity<>(user, HttpStatus.OK);
            }
        } catch (UserNotFoundException e) {
            LOGGER.error(USER_NOT_FOUND);
            throw e;
        }

        return new ResponseEntity<>(USER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserAndPostsDto> addUser(@Validated @RequestBody UserAndPostsDto dto) throws Exception {

        try {
            return new ResponseEntity<>(userService.addUser(dto), HttpStatus.CREATED);
        } catch (UserInvalidException e) {
            LOGGER.error("User invalid");
            throw e;
        } catch (UserAlreadyExistsException e) {
            LOGGER.error("User already exists");
            throw e;
        }

    }

    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserAndPostsDto> changeUser(@RequestBody UserAndPostsDto dto) throws Exception {

        try {
            return new ResponseEntity<>(userService.changeUser(dto), HttpStatus.OK);
        } catch (UserNotFoundException e) {
            LOGGER.error(USER_NOT_FOUND);
            throw e;
        }

    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestParam Long id) throws Exception {
        userService.deleteUser(id);
        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }

    @PostMapping(value="/fetch", produces = "application/json")
    public ResponseEntity<String> fetchUsersFromAPI() throws Exception {
        userService.fetchAllUsersFromApi();
        return new ResponseEntity<>("Users and posts retrieved", HttpStatus.CREATED);
    }

}
