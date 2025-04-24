package com.angelo.demo.controller;

import com.angelo.demo.common.dto.UserAndPostsDto;
import com.angelo.demo.common.model.Address;
import com.angelo.demo.common.model.Company;
import com.angelo.demo.exception.UserAlreadyExistsException;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.user.UserController;
import com.angelo.demo.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private UserAndPostsDto userDto1;
    private UserAndPostsDto userDto2;
    private List<UserAndPostsDto> userDtoList;

    @BeforeEach
    void setUp() {
        userDto1 = createUserDto(1L, "User One", "userone", "one@example.com", 1);
        userDto2 = createUserDto(2L, "User Two", "usertwo", "two@example.com", 0);
        userDtoList = Arrays.asList(userDto1, userDto2);
    }

    private UserAndPostsDto createUserDto(Long id, String fullName, String userName, String email, int postCount) {
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(id); dto.setFullName(fullName); dto.setUserName(userName); dto.setEmail(email);
        Address address = new Address(); address.setStreet("Street"); dto.setAddress(address);
        Company company = new Company(); company.setName("Comp"); dto.setCompany(company);
        List<Post> posts = new java.util.ArrayList<>();
        for (int i=0; i<postCount; i++) {
            Post p = new Post();
            p.setId(id*100 + i);
            p.setUserId(id);
            p.setTitle("Post " + i); posts.add(p);
        }
        dto.setPosts(posts);
        return dto;
    }

    @Test
    void getAllUsers_shouldReturnOkAndUserList() throws Exception {
        when(userService.getAllUsers()).thenReturn(userDtoList);

        mockMvc.perform(get("/users")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].fullName", is("User One")))
                .andExpect(jsonPath("$[1].id", is(2)))
                .andExpect(jsonPath("$[1].fullName", is("User Two")));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllUsers_shouldReturnNotFoundWhenNoUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/users")
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Users not found"));

        verify(userService).getAllUsers();
    }

    @Test
    void findUserById_shouldReturnOkAndUserWhenFound() throws Exception {
        Long id = 1L;
        when(userService.getUserById(id)).thenReturn(userDto1);

        mockMvc.perform(get("/users/find-by-id")
                        .param("id", String.valueOf(id))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("User One")));

        verify(userService).getUserById(id);
    }

    @Test
    void findUserById_shouldReturnNotFoundWhenUserNotFoundException() throws Exception {
        Long id = 99L;
        when(userService.getUserById(id)).thenThrow(new UserNotFoundException("User with id 99 not found"));

        mockMvc.perform(get("/users/find-by-id")
                        .param("id", String.valueOf(id))
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User with id 99 not found"));

        verify(userService).getUserById(id);
    }

//    @Test
//    void addUser_shouldReturnCreatedAndUserDtoOnSuccess() throws Exception {
//        UserAndPostsDto inputDto = createUserDto(null, "New User", "newuser", "new@example.com", 1);
//        UserAndPostsDto savedDto = createUserDto(5L, "New User", "newuser", "new@example.com", 1); // Simulate ID assigned
//        when(userService.addUser(any(UserAndPostsDto.class))).thenReturn(savedDto);
//
//        mockMvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputDto))
//                        .with(jwt()))
//                .andExpect(status().isCreated())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$.id", is(5)))
//                .andExpect(jsonPath("$.userName", is("newuser")));
//
//        verify(userService).addUser(any(UserAndPostsDto.class));
//    }

//    @Test
//    void addUser_shouldReturnBadRequestWhenUserInvalidException() throws Exception {
//        UserAndPostsDto inputDto = createUserDto(null, null, "newuser", "new@example.com", 1); // Invalid: null name
//        when(userService.addUser(any(UserAndPostsDto.class)))
//                .thenThrow(new UserInvalidException("Full name required"));
//
//        mockMvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputDto))
//                        .with(jwt()))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("Full name required"));
//
//        verify(userService).addUser(any(UserAndPostsDto.class));
//    }

//    @Test
//    void addUser_shouldReturnBadRequestWhenUserAlreadyExistsException() throws Exception {
//        UserAndPostsDto inputDto = createUserDto(null, "Existing User", "existing", "exist@example.com", 2);
//        when(userService.addUser(any(UserAndPostsDto.class)))
//                .thenThrow(new UserAlreadyExistsException("User with userName existing exists"));
//
//        mockMvc.perform(post("/users")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(inputDto))
//                        .with(jwt()))
//                .andExpect(status().isBadRequest())
//                .andExpect(content().string("User with userName existing exists"));
//
//        verify(userService).addUser(any(UserAndPostsDto.class));
//    }


    @Test
    void changeUser_shouldReturnOkAndUpdatedDtoOnSuccess() throws Exception {
        Long id = 1L;
        UserAndPostsDto inputDto = createUserDto(id, "Updated User", "userone", "update@example.com", 1);
        UserAndPostsDto updatedDto = inputDto; // Assume service returns the updated DTO
        when(userService.changeUser(any(UserAndPostsDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.fullName", is("Updated User")))
                .andExpect(jsonPath("$.email", is("update@example.com")));

        verify(userService).changeUser(any(UserAndPostsDto.class));
    }

    @Test
    void changeUser_shouldReturnNotFoundWhenUserNotFoundException() throws Exception {
        Long id = 99L;
        UserAndPostsDto inputDto = createUserDto(id, "Non Existent", "nouser", "no@example.com", 0);
        when(userService.changeUser(any(UserAndPostsDto.class)))
                .thenThrow(new UserNotFoundException("User to update not found"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User to update not found"));

        verify(userService).changeUser(any(UserAndPostsDto.class));
    }

    @Test
    void changeUser_shouldReturnBadRequestWhenUserInvalidException() throws Exception {
        Long id = 1L;
        UserAndPostsDto inputDto = createUserDto(id, null, "userone", "update@example.com", 1); // Invalid null name
        when(userService.changeUser(any(UserAndPostsDto.class)))
                .thenThrow(new UserInvalidException("Full name required"));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Full name required"));

        verify(userService).changeUser(any(UserAndPostsDto.class));
    }


    @Test
    void deleteUser_shouldReturnOkOnSuccess() throws Exception {
        Long id = 1L;
        doNothing().when(userService).deleteUser(id);

        mockMvc.perform(delete("/users")
                        .param("id", String.valueOf(id))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));

        verify(userService).deleteUser(id);
    }

    @Test
    void deleteUser_shouldReturnNotFoundWhenUserNotFoundException() throws Exception {
        Long id = 99L;
        doThrow(new UserNotFoundException("User not found")).when(userService).deleteUser(id);

        mockMvc.perform(delete("/users")
                        .param("id", String.valueOf(id))
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("User not found"));

        verify(userService).deleteUser(id);
    }

    @Test
    void fetchUsersFromAPI_shouldReturnCreatedOnSuccess() throws Exception {
        doNothing().when(userService).fetchAllUsersFromApi();

        mockMvc.perform(post("/users/fetch")
                        .with(jwt()))
                .andExpect(status().isCreated()) // Status is CREATED based on controller logic
                .andExpect(content().string("Users and posts retrieved"));

        verify(userService).fetchAllUsersFromApi();
    }

//    @Test
//    void fetchUsersFromAPI_shouldPropagateExceptionFromService() throws Exception {
//        // Simulate an error during fetch (e.g., API unreachable)
//        doThrow(new RuntimeException("API fetch failed")).when(userService).fetchAllUsersFromApi();
//
//        // Expect the controller to let the exception propagate (handled by global handler or default Spring error handling)
//        // We don't explicitly check for 500 here, as the exact handling depends on the full context,
//        // but we verify the service method was called and threw.
//        mockMvc.perform(post("/users/fetch")
//                        .with(jwt()))
//                .andExpect(status().isInternalServerError()); // Assuming default handling leads to 500
//
//        verify(userService).fetchAllUsersFromApi();
//    }
}