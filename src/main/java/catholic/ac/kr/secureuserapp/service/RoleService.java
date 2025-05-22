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
        apiResponse.setMessage("Roles found");
        apiResponse.setData(roleDTOS);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> saveRole(Role role) {
        Optional<Role> roleOptional = roleRepository.findByName(role.getName());
        if (roleOptional.isPresent()) {
            ApiResponse<Role> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Role already exist.");

            return ResponseEntity.status(HttpStatus.CONFLICT).body(apiResponse);
        }

        Role newRole = Role.builder()
                .name(role.getName())
                .build();

        roleRepository.save(newRole);

        ApiResponse<Role> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Role created");
        apiResponse.setData(newRole);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(Long id, RoleDTO roleDTO) {
        Optional<Role> roleOptional = roleRepository.findById(id); //tìm theo id
        if (roleOptional.isEmpty()) {
            ApiResponse<RoleDTO> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Role not found.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }

        Role existingRole = roleOptional.get(); //lấy Role từ Optional
        existingRole.setName(roleDTO.getName()); //gán role mới
        Role updatedRole = roleRepository.save(existingRole); //lưu lại vào DB

        ApiResponse<RoleDTO> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Role updated");
        apiResponse.setData(roleMapper.toDTO(updatedRole)); //map user sang dto

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Role>> deleteRole(Long id) {
        Role role = roleRepository.findById(id).orElse(null);
        if (role == null) {
            ApiResponse<Role> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Role not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);

        }

        roleRepository.deleteById(role.getId());

        ApiResponse<Role> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Delete Role success");

        return ResponseEntity.ok(apiResponse);
    }
}
