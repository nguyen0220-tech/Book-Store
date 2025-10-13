package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PostDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.PostRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("post")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getAllPostsByUserId(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(postService.getAllPostsByUserId(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("deleted")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getAllPostsDeletedByUserId(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @RequestParam int page,
            @RequestParam int size){
        return ResponseEntity.ok(postService.getPostsDeletedStillRestorableByUserId(userDetails.getUser().getId(), page, size));
    }

    @GetMapping("all")
    public ResponseEntity<ApiResponse<Page<PostDTO>>> getAllPosts(
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(postService.getAllPosts(page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostDTO>> createPost(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @ModelAttribute PostRequest request){
        return ResponseEntity.ok(postService.createPost(userDetails.getUser().getId(), request));
    }

    @PutMapping("{postId}")
    public ResponseEntity<ApiResponse<PostDTO>> updatePost(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostRequest request){
        return ResponseEntity.ok(postService.updatePost(userDetails.getUser().getId(),postId, request));
    }

    @DeleteMapping("{postId}")
    public ResponseEntity<ApiResponse<String>> deletePost(@AuthenticationPrincipal MyUserDetails userDetails,@PathVariable Long postId){
        return ResponseEntity.ok(postService.deletePost(userDetails.getUser().getId(),postId));
    }

    @PutMapping("{postId}/restore")
    public ResponseEntity<ApiResponse<String>> restorePost(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId){
        return ResponseEntity.ok(postService.restorePost(userDetails.getUser().getId(),postId));
    }
}
