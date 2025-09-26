package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.dto.request.PostEmotionRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.PostEmotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("posts/{postId}/emotions")
@RequiredArgsConstructor
public class PostEmotionController {
    private final PostEmotionService postEmotionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostEmotionCountDTO>>> getPostEmotionCount(@PathVariable Long postId) {
        return ResponseEntity.ok(postEmotionService.getPostEmotionAndCount(postId));

    }

    @GetMapping("filter")
    public ResponseEntity<ApiResponse<List<PostEmotionFilterUserDTO>>> getPostEmotionFilterUsers(@PathVariable Long postId) {
        return ResponseEntity.ok(postEmotionService.getPostEmotionAndFilter(postId));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PostEmotionDTO>> createPostEmotion(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostEmotionRequest request) {
        return ResponseEntity.ok(postEmotionService.createPostEmotion(userDetails.getUser().getId(), postId,request.getEmotionStatus()));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<PostEmotionDTO>> updatePostEmotion(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId,
            @RequestBody PostEmotionRequest request){
        return ResponseEntity.ok(postEmotionService.updatePostEmotion(userDetails.getUser().getId(), postId,request.getEmotionStatus()));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> deletePostEmotion(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @PathVariable Long postId) {
        return ResponseEntity.ok(postEmotionService.deletePostEmotion(userDetails.getUser().getId(), postId));
    }
}
