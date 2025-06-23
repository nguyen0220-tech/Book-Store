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
@RequestMapping("roles")
@RequiredArgsConstructor
public class RoleController {
    public final RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAllRoles());
    }

    @PostMapping("update/{id}")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRole(@PathVariable("id") Long id, @Valid @RequestBody RoleDTO roleDTO) {
        return ResponseEntity.ok(roleService.updateRole(id, roleDTO));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<Role>> addRole(@Valid @RequestBody Role role) {
        return ResponseEntity.ok(roleService.saveRole(role));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<Role>> deleteRole(@PathVariable("id") Long id) {
       return ResponseEntity.ok(roleService.deleteRole(id));


    }



}
