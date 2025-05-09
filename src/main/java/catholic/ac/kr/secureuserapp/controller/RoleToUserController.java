package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.RoleToUserRequest;
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
    public ResponseEntity<?> getAllRolesOfUser(@PathVariable String username ) {
        Set<Role> roles=roleToUserService.getRoleOfUser(username);

        return ResponseEntity.ok(roles);
    }

    @PostMapping("add-role")
    public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserRequest request){
        roleToUserService.addRoleToUser(request.getUsername(),request.getRoleName());
        return ResponseEntity.ok("Role added");
    }

    @DeleteMapping("/{username}/rem-role/{roleName}")
    public ResponseEntity<?> removeRoleToUser(@PathVariable String username,@PathVariable String roleName) {
        roleToUserService.removeRoleFromUser(username,roleName);

        return ResponseEntity.ok("Role removed");
    }

}
