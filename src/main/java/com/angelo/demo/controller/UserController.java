package com.angelo.demo.controller;

import com.angelo.demo.entity.User;
import com.angelo.demo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping
    public List<User> getAllUsers() throws Exception { return userService.getAllUsers(); }

    @GetMapping("/find-by-id")
    public ResponseEntity<Object> findUserById(@RequestParam Long id) throws Exception{
        Optional<User> user = userService.getUserById(id);
        return user.<ResponseEntity<Object>>map(res ->
                new ResponseEntity<>(res, HttpStatus.OK)).orElseGet(() ->
                new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<User> addUser(@RequestBody User user) throws Exception {
        return new ResponseEntity<>(userService.addUser(user), HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<User> changeUser(@RequestParam Long id, @RequestBody User user) throws Exception {
        user.setId(id);

        return new ResponseEntity<>(userService.changeUser(user), HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestParam Long id) throws Exception {
        userService.deleteUser(id);
        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }

    @PostMapping("/fetch")
    public ResponseEntity<String> fetchUsersFromAPI() throws Exception {
        userService.fetchAllUsersFromApi();
        return new ResponseEntity<>("Users and posts retrieved", HttpStatus.CREATED);
    }

}
