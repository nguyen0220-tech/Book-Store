package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserDTO {
    @NotBlank(message = "Tên không được để trống")
    private String username;

    @NotBlank(message = "ROLE không được để trống")
    private String role;
}
