package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.PostShare;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.PostMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PostDTO;
import catholic.ac.kr.secureuserapp.model.dto.PostRequest;
import catholic.ac.kr.secureuserapp.model.entity.Post;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.PostRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Cacheable(value = "postCache", key = "#userId")
    public ApiResponse<Page<PostDTO>> getAllPostsByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("postDate").descending());
        Page<Post> posts = postRepository.findByUserId(userId, pageable);

        Page<PostDTO> postDTOS = posts.map(PostMapper::toPostDTO);

        return ApiResponse.success("posts", postDTOS);
    }

    @Cacheable(value = "postCache", key = "{#page, #size}")
    public ApiResponse<Page<PostDTO>> getAllPosts(int page,int size ) {
        Pageable pageable = PageRequest.of(page, size,Sort.by("postDate").descending());
        Page<Post> posts = postRepository.findAll(pageable);

        Page<PostDTO> postDTOS = posts.map(PostMapper::toPostDTO);

        return ApiResponse.success("All posts", postDTOS);
    }

    @CacheEvict(value = "postCache", allEntries = true)
    public ApiResponse<PostDTO> createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Post post = new Post();
        post.setUser(user);
        post.setContent(request.getContent());
        post.setPostDate(new Timestamp(System.currentTimeMillis()));
        post.setPostShare(PostShare.valueOf(request.getPostShare()));
        post.setImageUrl(request.getImageUrl());

        postRepository.save(post);

        PostDTO savedPostDTO = PostMapper.toPostDTO(post);

        return ApiResponse.success("post", savedPostDTO);
    }

    @CacheEvict(value = "postCache", allEntries = true)
    public ApiResponse<PostDTO> updatePost(Long userId,Long postId,PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findByUserIdAndPostId(user.getId(),postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found"));

        post.setContent(request.getContent());
        post.setPostShare(PostShare.valueOf(request.getPostShare()));
        post.setImageUrl(request.getImageUrl());

        postRepository.save(post);
        PostDTO savedPostDTO = PostMapper.toPostDTO(post);
        return ApiResponse.success("updated post", savedPostDTO);
    }

    @CacheEvict(value = "postCache", allEntries = true)
    public ApiResponse<String> deletePost(Long userId,Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findByUserIdAndPostId(user.getId(),postId)
                .orElseThrow(()-> new ResourceNotFoundException("Post not found"));

        postRepository.delete(post);

        return ApiResponse.success("deleted post");
    }
}
