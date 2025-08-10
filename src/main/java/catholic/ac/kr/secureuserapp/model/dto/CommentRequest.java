package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class CommentRequest {
    private Long postId;
    private String comment;
}
