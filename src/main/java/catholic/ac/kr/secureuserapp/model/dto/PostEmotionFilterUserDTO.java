package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.EmotionStatus;

public record PostEmotionFilterUserDTO (
        Long userId,
        String username,
        EmotionStatus emotionStatus
){}