package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter @Setter
public class PostDTO {
    private Long id;
    private Long userId;
    private String username;
    private String content;
    private String postShare;
    private Timestamp postDate;
    private String imageUrl;
    private boolean deleted;
    private LocalDateTime expiryRestore;
}
