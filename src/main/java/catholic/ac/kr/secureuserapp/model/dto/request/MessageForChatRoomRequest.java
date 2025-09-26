package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageForChatRoomRequest {
    private String sender;
    private Long chatRoomId;
    private String message;

}
