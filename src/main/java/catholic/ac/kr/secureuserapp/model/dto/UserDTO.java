package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class UserDTO {
    @NotBlank(message = "Tên không được để trống")
     String username;

    @NotBlank(message = "ROLE không được để trống")
     String role;
}
