package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.Status.Sex;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {
    @Pattern(regexp = "^[A-Za-z ]+$", message = "Tên không hợp lệ")
    private String fullName;
    private String address;
    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    private String liking;
    private Sex sex;
}
