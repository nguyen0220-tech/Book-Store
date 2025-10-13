package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.Rating;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

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
    private MultipartFile file;
    private Timestamp createdAt;
    private int rating;
}
