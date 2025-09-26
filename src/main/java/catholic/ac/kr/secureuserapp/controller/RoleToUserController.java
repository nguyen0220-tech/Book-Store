package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.request.RoleToUserRequest;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.service.RoleToUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class RoleToUserController {
    private final RoleToUserService roleToUserService;

    @GetMapping("/{username}/roles")
    public ResponseEntity<ApiResponse<Set<Role>>> getAllRolesOfUser(@PathVariable String username) {
        ApiResponse<Set<Role>> roles = roleToUserService.getRoleOfUser(username);

        return ResponseEntity.ok(roles);
    }

    @PostMapping("/add-role")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserRequest request) {
        return ResponseEntity.ok(roleToUserService.addRoleToUser(request.getUsername(), request.getRoleName()));
    }

    @DeleteMapping("/{username}/rem-role/{roleName}")
    public ResponseEntity<ApiResponse<?>> removeRoleFromUser(@PathVariable String username, @PathVariable String roleName) {
        return ResponseEntity.ok(roleToUserService.removeRoleFromUser(username, roleName));
    }

}
