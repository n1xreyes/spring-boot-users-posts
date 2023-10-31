package com.angelo.demo.controller;

import com.angelo.demo.dto.UserAndPostsDto;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get all users with their posts from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found all users with their posts",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "404", description = "Users not found", content = @Content),
    })
    @GetMapping(produces = "application/json")
    public ResponseEntity<Object> getAllUsers() throws Exception {
        List<UserAndPostsDto> userAndPostsDtos = userService.getAllUsers();
        if (!userAndPostsDtos.isEmpty()) {
            return new ResponseEntity<>(userAndPostsDtos, HttpStatus.OK);
        }

        return new ResponseEntity<>("Users not found", HttpStatus.NOT_FOUND);
    }

    @Operation(summary = "Get by id from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found users with their posts",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "404", description = "Users not found", content = @Content),
    })
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

    @Operation(summary = "Add a new user to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "New user with their posts saved to the database",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "400", description = "User invalid. Either it exists or there are invalid fields", content = @Content),
    })
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

    @Operation(summary = "Update an existing user in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existing user updated in the database",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "400", description = "User invalid. There are invalid fields", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PutMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<UserAndPostsDto> changeUser(@RequestBody UserAndPostsDto dto) throws Exception {

        try {
            return new ResponseEntity<>(userService.changeUser(dto), HttpStatus.OK);
        } catch (UserNotFoundException e) {
            LOGGER.error(USER_NOT_FOUND);
            throw e;
        }

    }

    @Operation(summary = "Delete an existing user from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existing user deleted from the database",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestParam Long id) throws Exception {
        userService.deleteUser(id);
        return new ResponseEntity<>("User deleted", HttpStatus.OK);
    }

    @Operation(summary = "Perform an external API call to retrieve users and posts, and persist in the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existing user updated in the database",
                    content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserAndPostsDto.class))}),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    })
    @PostMapping(value="/fetch", produces = "application/json")
    public ResponseEntity<String> fetchUsersFromAPI() throws Exception {
        userService.fetchAllUsersFromApi();
        return new ResponseEntity<>("Users and posts retrieved", HttpStatus.CREATED);
    }

}
