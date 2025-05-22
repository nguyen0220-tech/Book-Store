package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.mapper.RoleMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RoleDTO;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> findAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO>  roleDTOS = roleMapper.toRole(roles);

        ApiResponse<List<RoleDTO>> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Lấy danh sách role thành công.");
        apiResponse.setData(roleDTOS);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> saveRole(Role role) {
        Role newRole = Role.builder()
                .name(role.getName())
                .build();

        roleRepository.save(newRole);

        ApiResponse<Role> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Thêm role thành công");
        apiResponse.setData(newRole);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateRole(Long id, Role role) {
        Optional<Role> roleOptional = roleRepository.findById(id);
        if (roleOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Role not found");
        }

        Role existingRole = roleOptional.get();
        existingRole.setName(role.getName());

        Role updatedRole = roleRepository.save(existingRole);
        return ResponseEntity.ok(updatedRole);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> deleteRole(Long id) {
        Role role = roleRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Role not found"));
        roleRepository.deleteById(role.getId());

        ApiResponse<Role> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Xóa role thành công");

        return ResponseEntity.ok(apiResponse);
    }
}
