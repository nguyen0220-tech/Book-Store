package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.Status.Rating;
import catholic.ac.kr.secureuserapp.model.dto.ReviewDTO;
import catholic.ac.kr.secureuserapp.model.entity.Review;

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
        reviewDTO.setRating(Rating.getValue(review.getRating()));

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
}
