package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.model.entity.User;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
public class ChatRoomRequest {
    private Set<Long> memberIds;
    private String chatRoomName;
}
