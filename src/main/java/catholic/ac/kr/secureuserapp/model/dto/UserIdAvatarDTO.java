package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserIdAvatarDTO {
    private Map<Long, String> userIdAvatar;
}
