package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class ReviewDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private Long orderId;
    private String author;
    private String content;
    private Timestamp createdAt;
}
