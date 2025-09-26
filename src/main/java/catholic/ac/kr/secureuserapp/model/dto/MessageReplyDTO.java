package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageReplyDTO {
    private Long messageId;
//    private String replyText;
    private String replyUser;
    private String messageReply;

}
