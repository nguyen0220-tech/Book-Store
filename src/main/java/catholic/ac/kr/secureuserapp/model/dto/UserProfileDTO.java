package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter
public class UserProfileDTO {
    private String username;
    private String fullName;
    private String address;
    private String phone;
    private String liking;
    private String sex;
}
