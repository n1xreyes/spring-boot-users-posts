package com.angelo.demo.mapper;

import com.angelo.demo.common.dto.UserAndPostsDto;
import com.angelo.demo.common.model.Address;
import com.angelo.demo.common.model.Company;
import com.angelo.demo.post.dto.PostDto;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperTest {
    private Mapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new Mapper();
    }

    private User createUser(Long id, String name, String username, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone("123-456-7890");
        user.setWebsite("example.com");

        Address address = new Address();
        address.setStreet("123 Main St");
        address.setSuite("Apt 4B");
        address.setCity("Anytown");
        address.setZipcode("12345");
        user.setAddress(address);

        Company company = new Company();
        company.setName("Test Inc.");
        company.setCatchPhrase("Testing is fun");
        company.setBs("synergize testing solutions");
        user.setCompany(company);

        return user;
    }

    private Post createPost(Long id, Long userId, String title, String body) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(title);
        post.setBody(body);
        return post;
    }

    private UserAndPostsDto createUserDto(Long id, String fullName, String userName, String email) {
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(id);
        dto.setFullName(fullName);
        dto.setUserName(userName);
        dto.setEmail(email);
        dto.setPhone("123-456-7890");
        dto.setWebsite("example.com");

        Address address = new Address();
        address.setStreet("123 Main St");
        address.setSuite("Apt 4B");
        address.setCity("Anytown");
        address.setZipcode("12345");
        dto.setAddress(address);

        Company company = new Company();
        company.setName("Test Inc.");
        company.setCatchPhrase("Testing is fun");
        company.setBs("synergize testing solutions");
        dto.setCompany(company);

        return dto;
    }

    private PostDto createPostDto(String title, String body) {
        PostDto postDto = new PostDto();
        postDto.setTitle(title);
        postDto.setBody(body);
        return postDto;
    }

    @Test
    void toDto_UserAndPosts_shouldMapCorrectly() {
        User user = createUser(1L, "Test User", "testuser", "test@example.com");
        Post post1 = createPost(101L, 1L, "Title 1", "Body 1");
        Post post2 = createPost(102L, 1L, "Title 2", "Body 2");
        List<Post> posts = Arrays.asList(post1, post2);

        UserAndPostsDto dto = mapper.toDto(user, posts);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getFullName());
        assertEquals(user.getUsername(), dto.getUserName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getPhone(), dto.getPhone());
        assertEquals(user.getWebsite(), dto.getWebsite());

        assertNotNull(dto.getAddress());
        assertEquals(user.getAddress().getStreet(), dto.getAddress().getStreet());
        assertEquals(user.getAddress().getSuite(), dto.getAddress().getSuite());
        assertEquals(user.getAddress().getCity(), dto.getAddress().getCity());
        assertEquals(user.getAddress().getZipcode(), dto.getAddress().getZipcode());

        assertNotNull(dto.getCompany());
        assertEquals(user.getCompany().getName(), dto.getCompany().getName());
        assertEquals(user.getCompany().getCatchPhrase(), dto.getCompany().getCatchPhrase());
        assertEquals(user.getCompany().getBs(), dto.getCompany().getBs());

        assertNotNull(dto.getPosts());
        assertEquals(2, dto.getPosts().size());
        assertSame(post1, dto.getPosts().get(0));
        assertSame(post2, dto.getPosts().get(1));
    }

    @Test
    void toDto_UserOnly_shouldMapCorrectly() {
        User user = createUser(1L, "Test User", "testuser", "test@example.com");

        UserAndPostsDto dto = mapper.toDto(user);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getName(), dto.getFullName());
        assertEquals(user.getUsername(), dto.getUserName());
        assertEquals(user.getEmail(), dto.getEmail());
        assertNull(dto.getPosts());
    }


    @Test
    void toDto_UserAndPosts_withNullPosts() {
        User user = createUser(1L, "Test User", "testuser", "test@example.com");

        UserAndPostsDto dto = mapper.toDto(user, null);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertNull(dto.getPosts());
    }

    @Test
    void toDto_UserAndPosts_withEmptyPosts() {
        User user = createUser(1L, "Test User", "testuser", "test@example.com");
        List<Post> posts = Collections.emptyList();

        UserAndPostsDto dto = mapper.toDto(user, posts);

        assertNotNull(dto);
        assertEquals(user.getId(), dto.getId());
        assertNotNull(dto.getPosts());
        assertTrue(dto.getPosts().isEmpty());
    }

    @Test
    void dtoToUser_shouldMapCorrectly() {
        UserAndPostsDto dto = createUserDto(1L, "Test DTO User", "testdtouser", "testdto@example.com");
        Post post1 = createPost(101L, 1L, "Title 1", "Body 1");
        dto.setPosts(Collections.singletonList(post1));

        User user = mapper.dtoToUser(dto);

        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getFullName(), user.getName());
        assertEquals(dto.getUserName(), user.getUsername());
        assertEquals(dto.getEmail(), user.getEmail());
        assertEquals(dto.getPhone(), user.getPhone());
        assertEquals(dto.getWebsite(), user.getWebsite());

        assertNotNull(user.getAddress());
        assertEquals(dto.getAddress().getStreet(), user.getAddress().getStreet());
        assertEquals(dto.getAddress().getSuite(), user.getAddress().getSuite());
        assertEquals(dto.getAddress().getCity(), user.getAddress().getCity());
        assertEquals(dto.getAddress().getZipcode(), user.getAddress().getZipcode());

        assertNotNull(user.getCompany());
        assertEquals(dto.getCompany().getName(), user.getCompany().getName());
        assertEquals(dto.getCompany().getCatchPhrase(), user.getCompany().getCatchPhrase());
        assertEquals(dto.getCompany().getBs(), user.getCompany().getBs());
    }

    @Test
    void dtoToUser_withNullNestedObjects() {
        UserAndPostsDto dto = new UserAndPostsDto();
        dto.setId(1L);
        dto.setFullName("Minimal User");
        dto.setUserName("minimal");
        dto.setEmail("min@example.com");

        User user = mapper.dtoToUser(dto);

        assertNotNull(user);
        assertEquals(dto.getId(), user.getId());
        assertEquals(dto.getFullName(), user.getName());
        assertEquals(dto.getUserName(), user.getUsername());
        assertNull(user.getAddress());
        assertNull(user.getCompany());
    }

    @Test
    void dtoToPost_shouldMapCorrectly() {
        Long userId = 5L;
        PostDto postDto = createPostDto("Post DTO Title", "Post DTO Body");

        Post post = mapper.dtoToPost(userId, postDto);

        assertNotNull(post);
        assertNull(post.getId());
        assertEquals(userId, post.getUserId());
        assertEquals(postDto.getTitle(), post.getTitle());
        assertEquals(postDto.getBody(), post.getBody());
    }

    @Test
    void postToDto_shouldMapCorrectly() {
        Post post = createPost(10L, 1L, "Entity Post Title", "Entity Post Body");

        PostDto postDto = mapper.postToDto(post);

        assertNotNull(postDto);
        assertEquals(post.getTitle(), postDto.getTitle());
        assertEquals(post.getBody(), postDto.getBody());
    }
}
