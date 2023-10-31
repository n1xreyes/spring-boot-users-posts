package com.angelo.demo.impl;

import com.angelo.demo.config.RestTemplateClient;
import com.angelo.demo.dto.UserAndPostsDto;
import com.angelo.demo.entity.Post;
import com.angelo.demo.entity.User;
import com.angelo.demo.exception.UserInvalidException;
import com.angelo.demo.exception.UserNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.repository.PostRepository;
import com.angelo.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class UserServiceImplTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PostRepository postRepository;

    @Mock
    Mapper mapper;

    @Mock
    RestTemplateClient restTemplate;

    @InjectMocks
    UserServiceImpl userService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    @Test
    public void testGetAllUsers_Success() throws Exception {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        Post post1 = new Post();
        post1.setId(1L);
        post1.setUserId(user1.getId());

        User user2 = new User();
        user2.setId(2L);
        Post post2 = new Post();
        post2.setId(2L);
        post2.setUserId(user2.getId());

        List<User> users = Arrays.asList(user1, user2);
        List<Post> postsUser1 = Arrays.asList(post1);
        List<Post> postsUser2 = Arrays.asList(post2);

        UserAndPostsDto userAndPostsDto1 = new UserAndPostsDto();
        userAndPostsDto1.setId(user1.getId());
        userAndPostsDto1.setPosts(postsUser1);

        UserAndPostsDto userAndPostsDto2 = new UserAndPostsDto();
        userAndPostsDto2.setId(user2.getId());
        userAndPostsDto2.setPosts(postsUser2);


        when(userRepository.findAll()).thenReturn(users);
        when(mapper.toDto(user1)).thenReturn(userAndPostsDto1);
        when(mapper.toDto(user2)).thenReturn(userAndPostsDto2);
        when(postRepository.findByUserId(user1.getId())).thenReturn(postsUser1);
        when(postRepository.findByUserId(user2.getId())).thenReturn(postsUser2);

        // Act
        List<UserAndPostsDto> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());

        UserAndPostsDto dto1 = result.get(0);
        assertEquals(user1.getId(), dto1.getId());
        assertEquals(postsUser1.size(), dto1.getPosts().size());

        UserAndPostsDto dto2 = result.get(1);
        assertEquals(user2.getId(), dto2.getId());
        assertEquals(postsUser2.size(), dto2.getPosts().size());
    }

    @Test
    public void testGetAllUsers_Failure() throws Exception {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserAndPostsDto> result = userService.getAllUsers();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetUserById_Success() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        Post post = new Post();
        post.setId(1L);
        post.setUserId(user.getId());

        List<Post> posts = Arrays.asList(post);
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(user.getId());
        dto.setPosts(posts);

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(postRepository.findByUserId(user.getId())).thenReturn(posts);
        when(mapper.toDto(user, posts)).thenReturn(dto);

        // Act
        UserAndPostsDto result = userService.getUserById(user.getId());

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getPosts().size(), result.getPosts().size());
    }

    @Test
    public void testGetUserById_Failure() throws Exception {
        // Arrange
        Long id = 1L;

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        UserAndPostsDto result = userService.getUserById(id);

        // Assert
        assertNull(result);
    }

    @Test
    public void testAddUser_Success() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setName("Test Name");
        user.setEmail("test@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setUserId(user.getId());

        List<Post> posts = Arrays.asList(post);
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(user.getId());
        dto.setFullName(user.getName());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPosts(posts);

        when(userRepository.existsByUsername(dto.getUserName())).thenReturn(false);
        when(userRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(mapper.dtoToUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(mapper.toDto(user, posts)).thenReturn(dto);

        // Act
        UserAndPostsDto result = userService.addUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getUserName(), result.getUserName());
        assertEquals(dto.getEmail(), result.getEmail());
    }

    @Test
    public void testAddUser_Failure() throws Exception {
        // Arrange
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setUserName("testUser");
        dto.setEmail("test@example.com");

        when(userRepository.existsByUsername(dto.getUserName())).thenReturn(true);

        // Act and Assert
        assertThrows(UserInvalidException.class, () -> userService.addUser(dto));
    }

    @Test
    public void testChangeUser_Success() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setName("Test Name");
        user.setUsername("testUser");
        user.setEmail("test@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setUserId(user.getId());

        List<Post> posts = Arrays.asList(post);
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(user.getId());
        dto.setFullName(user.getName());
        dto.setUserName(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPosts(posts);

        when(userRepository.existsById(dto.getId())).thenReturn(true);
        when(mapper.dtoToUser(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(postRepository.findByUserId(user.getId())).thenReturn(posts);
        when(mapper.toDto(user, posts)).thenReturn(dto);

        // Act
        UserAndPostsDto result = userService.changeUser(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getId(), result.getId());
        assertEquals(dto.getUserName(), result.getUserName());
        assertEquals(dto.getEmail(), result.getEmail());
    }

    @Test
    public void testChangeUser_Failure() throws Exception {
        // Arrange
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(1L);

        when(userRepository.existsById(dto.getId())).thenReturn(false);

        // Act and Assert
        assertThrows(UserInvalidException.class, () -> userService.changeUser(dto));
    }

    @Test
    public void testDeleteUser_Success() throws Exception {
        // Arrange
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(true);

        // Act
        userService.deleteUser(id);

        // Assert
        verify(userRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteUser_Failure() throws Exception {
        // Arrange
        Long id = 1L;

        when(userRepository.existsById(id)).thenReturn(false);

        // Act and Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(id));
    }

    @Test
    public void testFetchAllUsersFromApi() throws Exception {
        // Arrange
        User user = new User();
        user.setId(1L);
        user.setUsername("testUser");
        user.setEmail("test@example.com");

        Post post = new Post();
        post.setId(1L);
        post.setUserId(user.getId());

        List<Post> posts = Arrays.asList(post);
        User[] users = new User[]{user};
        Post[] postsArray = posts.toArray(new Post[0]);

        ResponseEntity<User[]> userResponse = new ResponseEntity<>(users, HttpStatus.OK);
        ResponseEntity<Post[]> postResponse = new ResponseEntity<>(postsArray, HttpStatus.OK);

        when(restTemplate.getForEntity(anyString(), eq(User[].class))).thenReturn(userResponse);
        when(restTemplate.getForEntity(anyString(), eq(Post[].class))).thenReturn(postResponse);

        ReflectionTestUtils.setField(userService, "postsApi", "mockPostsApi");
        ReflectionTestUtils.setField(userService, "usersApi", "mockUsersApi");

        // Act
        userService.fetchAllUsersFromApi();

        // Assert
        verify(userRepository, times(1)).saveAll(Arrays.asList(users));
        verify(postRepository, times(1)).saveAll(posts);
    }
}