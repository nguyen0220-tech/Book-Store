package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("user")
    public ResponseEntity<ApiResponse<UserDTO>> getUserById(@RequestParam("id") Long id) {
        return ResponseEntity.ok(userService.findUserById(id));
    }

    @GetMapping("users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers());
    }

    @GetMapping("search-name")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsersByName(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ResponseEntity.ok(userService.findAllUsersByNamePaging(page, size, keyword));
    }

    @GetMapping("filter-role")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> getAllUsersByRole(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String role
    ) {
        return ResponseEntity.ok(userService.findAllUsersByRole(page, size, role));
    }

    @PostMapping("{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(@PathVariable long id, @RequestBody @Valid UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(id, userDTO));
    }

    @PostMapping("add")
    public ResponseEntity<ApiResponse<?>> addUser(@Valid @RequestBody User user) {
        return ResponseEntity.ok(userService.saveUser(user));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<ApiResponse<UserDTO>> deleteUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.deleteUser(id));
    }

    @PostMapping("signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(userService.signUp(request));
    }

    @GetMapping("verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(userService.verifyEmail(token));
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }


}
