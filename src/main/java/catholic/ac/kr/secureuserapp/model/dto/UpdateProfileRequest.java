package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.Sex;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    private String fullName;
    private String address;
    private String phone;
    private String liking;
    private Sex sex;
}
