package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

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

    @GetMapping("my-profile")
    public ResponseEntity<ApiResponse<UserProfileDTO>> getUserProfile(@AuthenticationPrincipal MyUserDetails userDetails) {
        return ResponseEntity.ok(userService.getUserProfile(userDetails.getUser().getId()));
    }

    @PutMapping("my-profile/update")
    public ResponseEntity<ApiResponse<UserProfileDTO>> updateUserProfile(
            @AuthenticationPrincipal MyUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(userDetails.getUser().getId(), request));
    }

    @PutMapping("/my-profile/setup-pass")
    public ResponseEntity<ApiResponse<?>> changePassword(@AuthenticationPrincipal MyUserDetails userDetails, @Valid @RequestBody ChangePasswordRequest request) {
        return ResponseEntity.ok(userService.changePassword(userDetails.getUser().getId(), request));
    }
}
