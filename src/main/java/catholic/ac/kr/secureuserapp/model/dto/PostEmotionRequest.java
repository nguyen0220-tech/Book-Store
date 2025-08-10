package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.EmotionStatus;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostEmotionRequest {
    private EmotionStatus emotionStatus;
}
