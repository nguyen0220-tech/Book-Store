package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class UserDTO {
    @NotBlank(message = "Tên không được để trống")
    private String username;

    private Set<String> roles;
}
