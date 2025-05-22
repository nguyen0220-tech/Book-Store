package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
//đại diện cho dữ liệu gửi từ client khi đăng nhập

@Data //Lombok tự sinh getter/setter, toString, equals, hashCode
public class LoginRequest {
    @NotBlank(message = "Email không được để trống")
    private String username;
    @NotBlank(message = "Mật khẩu không được để trống")
    private String password;
}
