package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.Rating;

import java.sql.Timestamp;

public record ReviewDetailDTO(
        String username,
        Rating rating,
        String content,
        String imageReviewUrl,
        Timestamp createdAt
) {}
