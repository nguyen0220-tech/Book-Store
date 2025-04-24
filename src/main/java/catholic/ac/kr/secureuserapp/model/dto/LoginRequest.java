package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;
//đại diện cho dữ liệu gửi từ client khi đăng nhập
@Data
public class LoginRequest {
    private String username;
    private String password;
}
