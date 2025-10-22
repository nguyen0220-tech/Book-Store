package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class MessageDTO {
    private Long id;
    private String sender;
    private String senderFullName;
    private String recipient;
    private String recipientFullName;
    private String message;
    private String imageUrl;
    private Timestamp timestamp;
    private String status;
}
