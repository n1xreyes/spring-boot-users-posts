package com.angelo.demo.post;

import com.angelo.demo.post.dto.PostDto;
import com.angelo.demo.post.entity.Post;
import com.angelo.demo.exception.PostInvalidException;
import com.angelo.demo.exception.PostNotFoundException;
import com.angelo.demo.mapper.Mapper;
import com.angelo.demo.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    private static final String POSTS_PATH = "/posts";

    @Mock
    PostRepository postRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    Mapper mapper;

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpecMock;

    @Mock
    WebClient.ResponseSpec responseSpecMock;

    @InjectMocks
    PostService postService;

    @Test
    public void testFindAll() {
        // Arrange
        Post post = new Post();
        post.setId(1L);
        post.setTitle("test title");
        post.setBody("test body");
        List<Post> posts = Arrays.asList(post);

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setBody(post.getBody());
        List<PostDto> postDtos = Arrays.asList(postDto);

        when(postRepository.findAll()).thenReturn(posts);
        when(mapper.postToDto(post)).thenReturn(postDto);

        // Act
        List<PostDto> result = postService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(postDto.getTitle(), result.get(0).getTitle());
    }


    @Test
    public void testFindById_Success() {
        // Arrange
        Long id = 1L;
        Post post = new Post();
        post.setId(id);
        post.setTitle("test title");
        post.setBody("test body");

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setBody(post.getBody());

        when(postRepository.findById(id)).thenReturn(Optional.of(post));
        when(mapper.postToDto(post)).thenReturn(postDto);

        // Act
        PostDto result = postService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(postDto.getTitle(), result.getTitle());
    }

    @Test
    public void testFindById_Failure() {
        // Arrange
        Long id = 1L;

        when(postRepository.findById(id)).thenReturn(Optional.empty());

        // Act
        PostDto result = postService.findById(id);

        // Assert
        assertNull(result);
    }

    @Test
    public void testFindAllPostsByUserId() {
        // Arrange
        Long userId = 1L;
        Post post = new Post();
        post.setId(1L);
        post.setUserId(userId);
        post.setTitle("test title");
        post.setBody("test body");
        List<Post> posts = Arrays.asList(post);

        PostDto postDto = new PostDto();
        postDto.setTitle(post.getTitle());
        postDto.setBody(post.getBody());
        List<PostDto> postDtos = Arrays.asList(postDto);

        when(postRepository.findByUserId(userId)).thenReturn(posts);
        when(mapper.postToDto(post)).thenReturn(postDto);

        // Act
        List<PostDto> result = postService.findAllPostsByUserId(userId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(postDto.getTitle(), result.get(0).getTitle());
    }

    @Test
    public void testFindAllPostsByUserId_Failure() {
        // Arrange
        Long userId = 1L;

        when(postRepository.findByUserId(userId)).thenReturn(Collections.emptyList());

        // Act
        List<PostDto> result = postService.findAllPostsByUserId(userId);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSavePost_Success() {
        // Arrange
        Long userId = 1L;
        PostDto dto = new PostDto();
        dto.setTitle("test title");
        dto.setBody("test body");

        Post post = new Post();
        post.setId(1L);
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setBody(dto.getBody());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(mapper.dtoToPost(userId, dto)).thenReturn(post);
        when(postRepository.save(post)).thenReturn(post);
        when(mapper.postToDto(post)).thenReturn(dto);

        // Act
        PostDto result = postService.savePost(userId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getTitle(), result.getTitle());
    }

    @Test
    public void testSavePost_Failure() {
        // Arrange
        Long userId = 1L;
        PostDto dto = new PostDto();
        dto.setTitle("test title");
        dto.setBody("test body");

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act and Assert
        assertThrows(PostInvalidException.class, () -> postService.savePost(userId, dto));
    }


    @Test
    public void testUpdatePost_Success() {
        // Arrange
        Long id = 1L;
        Long userId = 1L;
        PostDto dto = new PostDto();
        dto.setTitle("test title");
        dto.setBody("test body");

        Post post = new Post();
        post.setId(id);
        post.setUserId(userId);
        post.setTitle(dto.getTitle());
        post.setBody(dto.getBody());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(postRepository.existsById(id)).thenReturn(true);
        when(postRepository.findByIdAndUserId(id, userId)).thenReturn(post);
        when(postRepository.findById(id)).thenReturn(Optional.of(post));
        when(postRepository.save(post)).thenReturn(post);
        when(mapper.postToDto(post)).thenReturn(dto);

        // Act
        PostDto result = postService.updatePost(id, userId, dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getTitle(), result.getTitle());
    }

    @Test
    public void testUpdatePost_Failure() {
        // Arrange
        Long id = 1L;
        Long userId = 1L;
        PostDto dto = new PostDto();
        dto.setTitle("test title");
        dto.setBody("test body");

        when(userRepository.existsById(userId)).thenReturn(false);

        // Act and Assert
        assertThrows(PostInvalidException.class, () -> postService.updatePost(id, userId, dto));
    }


    @Test
    public void testDeleteById_Success() throws Exception {
        // Arrange
        Long id = 1L;

        when(postRepository.existsById(id)).thenReturn(true);

        // Act
        postService.deleteById(id);

        // Assert
        verify(postRepository, times(1)).deleteById(id);
    }

    @Test
    public void testDeleteById_Failure() {
        // Arrange
        Long id = 1L;

        when(postRepository.existsById(id)).thenReturn(false);

        // Act and Assert
        assertThrows(PostNotFoundException.class, () -> postService.deleteById(id));
    }


    @Test
    public void testFetchAndSavePosts() throws Exception {
        // Arrange
        // --- Post Data ---
        Post post = new Post();
        post.setId(1L);
        post.setTitle("API Post Title");
        post.setBody("API Post Body");
        post.setUserId(10L);
        List<Post> posts = Arrays.asList(post);
        ResponseEntity<List<Post>> response = new ResponseEntity<>(posts, HttpStatus.OK);
        Mono<ResponseEntity<List<Post>>> postMono = Mono.just(response);

        when(webClient.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(POSTS_PATH)).thenReturn(requestHeadersSpecMock);
        when(requestHeadersSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.toEntityList(Post.class)).thenReturn(postMono);

        // Act
        postService.fetchAndSavePosts();

        // Assert
        // Verify the chain was called
        verify(webClient).get();
        verify(requestHeadersUriSpecMock).uri(POSTS_PATH);
        verify(requestHeadersSpecMock).retrieve();
        verify(responseSpecMock).toEntityList(Post.class);
        // Verify the repository interaction
        verify(postRepository, times(1)).saveAll(posts);
    }

}