package catholic.ac.kr.secureuserapp.service;

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

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> findAllRoles() {
        List<Role> roles = roleRepository.findAll();

        return ResponseEntity.ok(roles);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Role saveRole(Role role) {
        Role newRole = Role.builder()
                .name(role.getName())
                .build();

        return roleRepository.save(newRole);
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
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }
}
