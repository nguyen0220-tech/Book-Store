package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageReplyRequest {
    private String sender;
    private Long chatRoomId;
    private Long messageId;
    private String replyText;
}
