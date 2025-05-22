package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RoleDTO;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api-role")
@RequiredArgsConstructor
public class RoleController {
    public final RoleService roleService;

    @GetMapping("roles")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        return roleService.findAllRoles();
    }

    @PostMapping("{id}")
    public ResponseEntity<?> updateRole(@PathVariable("id") Long id, @Valid @RequestBody Role role) {
        return roleService.updateRole(id, role);
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<Role>> addRole(@Valid @RequestBody Role role) {
        return roleService.saveRole(role);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Role>> deleteRole(@PathVariable("id") Long id) {
       return roleService.deleteRole(id);


    }



}
