package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ImageDTO {
    private Long id;
    private String imageUrl;
    private LocalDateTime uploadAt;
}
