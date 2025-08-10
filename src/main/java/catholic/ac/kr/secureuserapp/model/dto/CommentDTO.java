package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class CommentDTO {
    private Long id;
    private Long postId;
    private Long userId;
    private String username;
    private String commentContent;
    private Timestamp createdAt;
}
