package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.Rating;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.ReviewMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDTO;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.*;
import lombok.RequiredArgsConstructor;
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


    public ApiResponse<List<ReviewDTO>> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        List<ReviewDTO> reviewDTOS = ReviewMapper.toReviewDTO(reviews);

        return ApiResponse.success("All reviews",reviewDTOS);
    }

    public ApiResponse<List<ReviewDTO>> getAllReviewsByBookId(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(()-> new ResourceNotFoundException("Book not found"));

        List<Review> reviews = reviewRepository.findByBookId(book.getId());

        List<ReviewDTO> reviewDTOS = ReviewMapper.toReviewDTO(reviews);

        return ApiResponse.success("All reviews",reviewDTOS);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<ReviewDTO> createReview(Long UserId,ReviewDTO reviewDTO) {
        User user = userRepository.findById(UserId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(reviewDTO.getOrderId())
                .orElseThrow(()-> new ResourceNotFoundException("Order not found"));

        Book book = bookRepository.findById(reviewDTO.getBookId())
                .orElseThrow(()-> new ResourceNotFoundException("Book not found"));

        boolean hasPurchased = orderRepository.existsByUserIdAndBookId(user.getId(), book.getId());

        if (!hasPurchased) {
            throw new ResourceNotFoundException("Order not found");
        }
        Review review = new Review();
        review.setUser(user);
        review.setOrder(order);
        review.setBook(book);
        review.setContent(reviewDTO.getContent());
        review.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        review.setRating(Rating.getRating(reviewDTO.getRating()));

        reviewRepository.save(review);

        OrderItem orderItem = orderItemRepository.findByOrderIdAndBookId(order.getId(),book.getId())
                .orElseThrow(()-> new ResourceNotFoundException("OrderItem not found"));

        orderItem.setReviewed(true);
        orderItemRepository.save(orderItem);

        ReviewDTO dto = ReviewMapper.toReviewDTO(review);

        return ApiResponse.success("Review uploaded",dto);

    }
}
