package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.model.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotBlank(message = "Tên không được để trống")
    private String username;

    private Set<RoleDTO> roles;
}
