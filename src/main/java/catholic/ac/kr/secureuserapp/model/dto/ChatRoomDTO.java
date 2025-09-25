package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class ChatRoomDTO {
    private Long id;
    private String chatRoomName;
    private Set<String> usernames = new HashSet<>();
}
