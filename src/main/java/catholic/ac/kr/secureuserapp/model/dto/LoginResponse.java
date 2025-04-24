package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

//dữ liệu phản hồi từ server khi đăng nhập thành công
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token; // JWT (JSON Web Token) – chuỗi dùng để xác thực người dùng trong các request tiếp theo
}
