package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FriendDTO {
    private Long id;
    private Long userId;
    private String username;
    private Long friendId;
    private String friendName;
    private String status;
}
