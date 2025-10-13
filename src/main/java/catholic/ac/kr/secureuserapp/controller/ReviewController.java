package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.Status.FilterReview;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDTO;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDetailDTO;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getAllReviews() {
        return ResponseEntity.ok(reviewService.getAllReviews());
    }

    //API hiển thị khi ở trang chủ
    @GetMapping("book")
    public ResponseEntity<ApiResponse<List<ReviewDTO>>> getAllReviewsByBookId(@RequestParam Long bookId) {
        return ResponseEntity.ok(reviewService.getAllReviewsByBookId(bookId));
    }

    //API hiển thị khi ở trang chi tiết sách
    @GetMapping("book/detail")
    public ResponseEntity<ApiResponse<Page<ReviewDetailDTO>>> getAllReviewsDetailByBookId(
            @RequestParam Long bookId,
            @RequestParam FilterReview filter,
            @RequestParam int page,
            @RequestParam int size) {
        return ResponseEntity.ok(reviewService.getReviewDetailByBookId(bookId,filter,page,size));
    }

    @PostMapping("upload")
    public ResponseEntity<ApiResponse<ReviewDTO>> addReview(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @ModelAttribute ReviewDTO reviewDTO) {
        return ResponseEntity.ok(reviewService.createReview(userDetails.getUser().getId(), reviewDTO));
    }
}
