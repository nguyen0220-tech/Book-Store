package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ActMemberToGroupRequest {
    private Long chatRoomId;
    private Long memberId;
    private Boolean act;
}
