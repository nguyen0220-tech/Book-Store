package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.RoleToUserRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("users")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return userService.findAllUsers();
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("search-name")
    public ResponseEntity<Page<UserDTO>> getAllUsersByName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword
    ) {
        return userService.findAllUsersByNamePaging(page, size, keyword);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @GetMapping("filter-role")
    public ResponseEntity<Page<UserDTO>> getAllUsersByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String role
    ) {
        return userService.findAllUsersByRole(page, size, role);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("{id}")
    public ResponseEntity<?> updateUser(@PathVariable long id, @RequestBody @Valid User user) {
        return userService.updateUser(id, user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        return userService.saveUser(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("add-role")
    public ResponseEntity<String> addRoleToUser(@RequestBody RoleToUserRequest request) {
        userService.addRoleToUser(request.getUsername(), request.getRoleName());
        return ResponseEntity.ok("Role added to user successfully");
    }

    @PostMapping("signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        return userService.signUp(request);
    }

    @GetMapping("verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        return userService.verifyEmail(token);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        return userService.login(request);
    }


}
