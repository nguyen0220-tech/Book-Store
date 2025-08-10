package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.EmotionStatus;

public record PostEmotionCountDTO(
        EmotionStatus emotionStatus,
        Long count
){}