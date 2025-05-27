package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.AlreadyExistsException;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.RoleMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RoleDTO;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
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
    public ApiResponse<List<RoleDTO>> findAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDTO>  roleDTOS = roleMapper.toRole(roles);

        return ApiResponse.success("Roles all found",roleDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> saveRole(Role role) {
        Optional<Role> roleOptional = roleRepository.findByName(role.getName());
        if (roleOptional.isPresent()) {
            throw new AlreadyExistsException(role.getName()+" already exists");
        }

        Role newRole = Role.builder()
                .name(role.getName())
                .build();

        roleRepository.save(newRole);

        return ApiResponse.success("Role created");
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<RoleDTO> updateRole(Long id, RoleDTO roleDTO) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        role.setName(roleDTO.getName());
        Role updatedRole = roleRepository.save(role);
        RoleDTO updatedRoleDTO=roleMapper.toDTO(updatedRole);

        return ApiResponse.success("Role updated", updatedRoleDTO);
    }


    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Role> deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        roleRepository.deleteById(role.getId());

        return ApiResponse.success("Role deleted");
    }
}
