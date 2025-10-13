package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CommentDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.CommentRequest;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CommentDTO>>> getAllComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.getAllComments(postId, page, size));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CommentDTO>> createComment(
            @AuthenticationPrincipal MyUserDetails user,
            @ModelAttribute CommentRequest request) {
        return ResponseEntity.ok(commentService.createComment(user.getUser().getId(), request));
    }

    @DeleteMapping("{commentId}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @AuthenticationPrincipal MyUserDetails user,
            @PathVariable Long commentId){
        return ResponseEntity.ok(commentService.deleteComment(user.getUser().getId(), commentId));
    }
}
