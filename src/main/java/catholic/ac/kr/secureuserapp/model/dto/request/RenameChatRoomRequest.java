package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RenameChatRoomRequest {
    private Long chatRoomId;
    private String newName;
}
