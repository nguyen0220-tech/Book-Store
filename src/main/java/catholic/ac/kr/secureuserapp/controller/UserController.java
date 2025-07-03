package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.service.FileService;
import catholic.ac.kr.secureuserapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final FileService fileService;

    @GetMapping("userid")
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

    @GetMapping("profile/{id}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserProfile(id));
    }

    @PutMapping("profile/update/{id}")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfile(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(id, request));
    }

    @PutMapping("/profile/setup-pass/{id}")
    public ResponseEntity<ApiResponse<?>> changePassword(@PathVariable Long id, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(id, request));
    }
}
