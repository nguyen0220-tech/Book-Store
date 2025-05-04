package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api-role")
@RequiredArgsConstructor
public class RoleController {
    public final RoleService roleService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return roleService.findAllRoles();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{id}")
    public ResponseEntity<?> updateRole(@PathVariable("id") Long id, @Valid @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Role addRole(@Valid @RequestBody Role role) {
        return roleService.saveRole(role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<?> deleteRole(@PathVariable("id") Long id) {
        roleService.deleteRole(id);

        return ResponseEntity.ok("Role deleted");
    }



}
