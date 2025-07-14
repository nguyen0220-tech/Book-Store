package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.ReviewDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.Order;
import catholic.ac.kr.secureuserapp.model.entity.Review;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.aspectj.weaver.ast.Or;

import java.util.ArrayList;
import java.util.List;

public class ReviewMapper {
    public static ReviewDTO toReviewDTO(Review review) {
        ReviewDTO reviewDTO = new ReviewDTO();
        reviewDTO.setId(review.getId());
        reviewDTO.setUserId(review.getUser().getId());
        reviewDTO.setUsername(review.getUser().getUsername());
        reviewDTO.setBookId(review.getBook().getId());
        reviewDTO.setBookTitle(review.getBook().getTitle());
        reviewDTO.setAuthor(review.getBook().getAuthor());
        reviewDTO.setOrderId(review.getOrder().getId());
        reviewDTO.setContent(review.getContent());
        reviewDTO.setCreatedAt(review.getCreatedAt());

        return reviewDTO;
    }

    public static List<ReviewDTO> toReviewDTO(List<Review> reviews) {
        if (reviews == null) {
            return null;
        }

        List<ReviewDTO> reviewDTOs = new ArrayList<>();
        for (Review review : reviews) {
            reviewDTOs.add(toReviewDTO(review));
        }
        return reviewDTOs;
    }

    public static Review toReview(ReviewDTO reviewDTO) {
        Review review = new Review();
        review.setId(reviewDTO.getId());
        review.setContent(reviewDTO.getContent());
        review.setCreatedAt(reviewDTO.getCreatedAt());

        User user = new User();
        user.setId(reviewDTO.getUserId());
        review.setUser(user);

        Book book = new Book();
        book.setId(reviewDTO.getBookId());
        review.setBook(book);

        Order order = new Order();
        order.setId(reviewDTO.getOrderId());
        review.setOrder(order);

        return review;
    }

}
