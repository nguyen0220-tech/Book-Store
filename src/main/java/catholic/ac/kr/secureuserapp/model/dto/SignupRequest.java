package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {
    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Vui lòng nhập năm sinh")
    private String yearOfBirth;

    @NotBlank(message = "Vui lòng nhập tháng sinh")
    private String monthOfBirth;

    @NotBlank(message = "Vui lòng nhập ngày sinh")
    private String dayOfBirth;

}
