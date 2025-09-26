package catholic.ac.kr.secureuserapp.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleToUserRequest {
    @NotBlank(message = "Tên không được để trống")
    private String username;

    @NotBlank(message = "Role không được để trống")
    private String roleName;
}
