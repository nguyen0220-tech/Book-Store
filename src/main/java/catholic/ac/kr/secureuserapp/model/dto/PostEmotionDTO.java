package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostEmotionDTO {
    private Long id;
    private Long userId;
    private String userName;
    private Long postId;
    private String emotionStatus;
}
