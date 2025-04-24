package com.angelo.demo.controller;

import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.post.PostController;
import com.angelo.demo.post.PostService;
import com.angelo.demo.post.dto.PostDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.when;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtDecoder jwtDecoder;

    private PostDto postDto1;
    private PostDto postDto2;
    private List<PostDto> postDtoList;

    @BeforeEach
    void setUp() {
        postDto1 = new PostDto();
        postDto1.setTitle("Title 1");
        postDto1.setBody("Body 1");

        postDto2 = new PostDto();
        postDto2.setTitle("Title 2");
        postDto2.setBody("Body 2");

        postDtoList = Arrays.asList(postDto1, postDto2);
    }

    @Test
    void getAllPosts_shouldReturnOkAndPostList() throws Exception {
        when(postService.findAll()).thenReturn(postDtoList);

        mockMvc.perform(get("/posts")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Title 1")))
                .andExpect(jsonPath("$[1].title", is("Title 2")));

        verify(postService).findAll();
    }

    @Test
    void getAllPosts_shouldReturnOkAndEmptyListWhenNoPosts() throws Exception {
        when(postService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/posts")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(postService).findAll();
    }


    @Test
    void findPost_shouldReturnOkAndPostDtoWhenFound() throws Exception {
        Long id = 1L;
        when(postService.findById(id)).thenReturn(postDto1);

        mockMvc.perform(get("/posts/{id}", id)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Title 1")))
                .andExpect(jsonPath("$.body", is("Body 1")));

        verify(postService).findById(id);
    }

    @Test
    void findPost_shouldReturnNotFoundWhenPostNotFoundException() throws Exception {
        Long id = 99L;
        when(postService.findById(id)).thenThrow(new PostNotFoundException("Post 99 not found"));

        mockMvc.perform(get("/posts/{id}", id)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post 99 not found"));

        verify(postService).findById(id);
    }

    @Test
    void findPost_shouldReturnNotFoundWhenServiceReturnsNull() throws Exception {
        Long id = 99L;
        when(postService.findById(id)).thenReturn(null);

        mockMvc.perform(get("/posts/{id}", id)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));

        verify(postService).findById(id);
    }

    @Test
    void getAllUserPosts_shouldReturnOkAndPostList() throws Exception {
        Long userId = 10L;
        when(postService.findAllPostsByUserId(userId)).thenReturn(postDtoList);

        mockMvc.perform(post("/posts")
                        .param("userId", String.valueOf(userId))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Title 1")));

        verify(postService).findAllPostsByUserId(userId);
    }

    @Test
    void getAllUserPosts_shouldReturnNotFoundWhenPostNotFoundException() throws Exception {
        Long userId = 99L;
        when(postService.findAllPostsByUserId(userId)).thenThrow(new PostNotFoundException("Posts for user 99 not found"));

        mockMvc.perform(post("/posts")
                        .param("userId", String.valueOf(userId))
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Posts for user 99 not found"));

        verify(postService).findAllPostsByUserId(userId);
    }

    @Test
    void addPost_shouldReturnCreatedAndPostDtoOnSuccess() throws Exception {
        Long userId = 10L;
        PostDto inputDto = new PostDto();
        inputDto.setTitle("New Post");
        inputDto.setBody("New Body");
        PostDto savedDto = inputDto;

        when(postService.savePost(eq(userId), any(PostDto.class))).thenReturn(savedDto);

        mockMvc.perform(post("/posts")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("New Post")))
                .andExpect(jsonPath("$.body", is("New Body")));

        verify(postService).savePost(eq(userId), any(PostDto.class));
    }

    @Test
    void addPost_shouldReturnBadRequestWhenPostInvalidException() throws Exception {
        Long userId = 99L;
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Invalid Post");
        inputDto.setBody("Invalid Body");

        when(postService.savePost(eq(userId), any(PostDto.class)))
                .thenThrow(new PostInvalidException("User with id 99 does not exist"));

        mockMvc.perform(post("/posts")
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("User with id 99 does not exist"));

        verify(postService).savePost(eq(userId), any(PostDto.class));
    }

    @Test
    void updatePost_shouldReturnOkAndUpdatedDtoOnSuccess() throws Exception {
        Long id = 1L;
        Long userId = 10L;
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Updated Title");
        inputDto.setBody("Updated Body");
        PostDto updatedDto = inputDto;

        when(postService.updatePost(eq(id), eq(userId), any(PostDto.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/posts")
                        .param("id", String.valueOf(id))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", is("Updated Title")))
                .andExpect(jsonPath("$.body", is("Updated Body")));

        verify(postService).updatePost(eq(id), eq(userId), any(PostDto.class));
    }

    @Test
    void updatePost_shouldReturnBadRequestWhenPostInvalidException() throws Exception {
        Long id = 1L;
        Long userId = 10L;
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Update Attempt");
        inputDto.setBody("Update Body");

        when(postService.updatePost(eq(id), eq(userId), any(PostDto.class)))
                .thenThrow(new PostInvalidException("Post with id 1 does not belong to userID 10"));

        mockMvc.perform(put("/posts")
                        .param("id", String.valueOf(id))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Post with id 1 does not belong to userID 10"));

        verify(postService).updatePost(eq(id), eq(userId), any(PostDto.class));
    }

    @Test
    void updatePost_shouldReturnNotFoundWhenPostNotFoundException() throws Exception {
        Long id = 99L;
        Long userId = 10L;
        PostDto inputDto = new PostDto();
        inputDto.setTitle("Update NonExistent");
        inputDto.setBody("Update Body");

        when(postService.updatePost(eq(id), eq(userId), any(PostDto.class)))
                .thenThrow(new PostNotFoundException("Post with id 99 does not exist"));

        mockMvc.perform(put("/posts")
                        .param("id", String.valueOf(id))
                        .param("userId", String.valueOf(userId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto))
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post with id 99 does not exist"));

        verify(postService).updatePost(eq(id), eq(userId), any(PostDto.class));
    }

    @Test
    void deletePost_shouldReturnOkOnSuccess() throws Exception {
        Long id = 1L;
        doNothing().when(postService).deleteById(id);

        mockMvc.perform(delete("/posts/{id}", id)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("Post deleted"));

        verify(postService).deleteById(id);
    }

    @Test
    void deletePost_shouldReturnNotFoundWhenPostNotFoundException() throws Exception {
        Long id = 99L;
        doThrow(new PostNotFoundException("Post not found")).when(postService).deleteById(id);

        mockMvc.perform(delete("/posts/{id}", id)
                        .with(jwt()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Post not found"));

        verify(postService).deleteById(id);
    }

    @Test
    void fetchPosts_shouldReturnOkOnSuccess() throws Exception {
        doNothing().when(postService).fetchAndSavePosts();

        mockMvc.perform(post("/posts/fetch")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().string("Posts retrieved from JSONPlaceholder API"));

        verify(postService).fetchAndSavePosts();
    }

//    @Test
//    void fetchPosts_shouldPropagateExceptionFromService() throws Exception {
//        doThrow(new RuntimeException("API fetch failed")).when(postService).fetchAndSavePosts();
//
//        mockMvc.perform(post("/posts/fetch")
//                        .with(jwt()))
//                .andExpect(status().isInternalServerError()); // Assume default handling leads to 500
//
//        verify(postService).fetchAndSavePosts();
//    }
}