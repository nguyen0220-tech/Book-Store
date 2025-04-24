package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;
//đại diện cho dữ liệu gửi từ client khi đăng nhập
<<<<<<< HEAD
@Data
=======
@Data //Lombok tự sinh getter/setter, toString, equals, hashCode
>>>>>>> 7b5a3ea9576dda0e2ba2152a997e3960ebd1e7c8
public class LoginRequest {
    private String username;
    private String password;
}
