package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class NotificationDTO {
    private Long id;
    private Long userId;
    private Long orderId;
    private String title;
    private String message;
    private boolean read;
    private Timestamp createdAt;
    private String type;
}
