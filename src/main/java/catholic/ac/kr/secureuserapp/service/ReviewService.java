package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FilterReview;
import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.Status.Rating;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ReviewMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDTO;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDetailDTO;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.*;
import catholic.ac.kr.secureuserapp.uploadhandler.UploadFileHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UploadFileHandler uploadFileHandler;
    private final ImageRepository imageRepository;


    public ApiResponse<List<ReviewDTO>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewDTO> reviewDTOS = ReviewMapper.toReviewDTO(reviews);

        return ApiResponse.success("All reviews", reviewDTOS);
    }

    public ApiResponse<List<ReviewDTO>> getAllReviewsByBookId(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        List<Review> reviews = reviewRepository.findByBookId(book.getId());

        List<ReviewDTO> reviewDTOS = ReviewMapper.toReviewDTO(reviews);

        return ApiResponse.success("All reviews", reviewDTOS);
    }

    public ApiResponse<Page<ReviewDetailDTO>> getReviewDetailByBookId(Long bookId, FilterReview filter, int page, int size) {
        Sort sort = null;
        switch (filter) {
            case REVIEW_NEW -> sort = Sort.by(Sort.Direction.DESC, "createdAt");
            case RATING_HIGH -> sort = Sort.by(Sort.Direction.DESC, "rating");
            case RATING_LOW -> sort = Sort.by(Sort.Direction.ASC, "rating");
        }

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ReviewDetailDTO> reviewDetailDTO = reviewRepository.getReviewDetailDTOByBookI(bookId, pageable);

        return ApiResponse.success("Review detail", reviewDetailDTO);

    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<ReviewDTO> createReview(Long userId, ReviewDTO reviewDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Book book = bookRepository.findById(reviewDTO.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Image imageReview = new Image();

        boolean hasPurchased = orderRepository.existsByUserIdAndBookId(user.getId(), book.getId());

        if (!hasPurchased) {
            throw new ResourceNotFoundException("Order not found");
        }
        Review review = new Review();
        review.setUser(user);
        review.setOrder(order);
        review.setBook(book);
        review.setContent(reviewDTO.getContent());

        String imageReviewUrl = reviewDTO.getFile() != null ?
                uploadFileHandler.uploadFile(userId,reviewDTO.getFile())
                : null;
        review.setImageReviewUrl(imageReviewUrl);

        review.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        review.setRating(Rating.getRating(reviewDTO.getRating()));

        reviewRepository.save(review);

        if (reviewDTO.getFile() != null) {
            imageReview.setUser(user);
            imageReview.setImageUrl(imageReviewUrl);
            imageReview.setSelected(false);
            imageReview.setType(ImageType.REVIEW);
            imageReview.setReferenceId(review.getId());

            imageRepository.save(imageReview);
        }

        OrderItem orderItem = orderItemRepository.findByOrderIdAndBookId(order.getId(), book.getId())
                .orElseThrow(() -> new ResourceNotFoundException("OrderItem not found"));

        orderItem.setReviewed(true);
        orderItemRepository.save(orderItem);

        ReviewDTO dto = ReviewMapper.toReviewDTO(review);

        return ApiResponse.success("Review uploaded", dto);

    }
}
