package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class MessageForGroupChatDTO {
    private String senderFullName;
    private String message;
    private Timestamp timestamp;
}
