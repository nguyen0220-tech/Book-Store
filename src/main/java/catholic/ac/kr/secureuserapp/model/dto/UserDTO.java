package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
<<<<<<< HEAD
import lombok.NoArgsConstructor;
import lombok.Value;
=======
>>>>>>> 7b5a3ea9576dda0e2ba2152a997e3960ebd1e7c8

@Data
public class UserDTO {
    @NotBlank(message = "Tên không được để trống")
    private String username;

    @NotBlank(message = "ROLE không được để trống")
    private String role;
}
