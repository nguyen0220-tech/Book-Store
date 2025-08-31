package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Value("${app.verification.base-url}")
    private String baseUrl;

    @PostMapping("signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @GetMapping("verify")
    public ResponseEntity<Boolean> verifyEmail(@RequestParam String token,
                                               HttpServletResponse response) throws IOException {

        boolean success = authService.verifyEmail(token);
        if (success) {
            response.sendRedirect(baseUrl + "/verify-user.html?success="+ true);
        } else {
            response.sendRedirect(baseUrl + "/verify-user.html?success="+false);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {
        return ResponseEntity.ok(authService.login(request, httpServletRequest));
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refresh_token");
        if (refreshTokenStr == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing refresh token"));
        }

        try {
            return ResponseEntity.ok(authService.refresh(refreshTokenStr));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("find-username")
    public ResponseEntity<ApiResponse<UserDTO>> findUserByPhone(@RequestBody FindUserRequest request) {
        return ResponseEntity.ok(authService.findUserNameByPhone(request.getPhoneNumber()));
    }

    @PostMapping("forget-pass")
    public ResponseEntity<ApiResponse<String>> forgetPassWord(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        if (username == null || username.isBlank()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing username"));
        }
        return ResponseEntity.ok(authService.forgetPassword(username));

    }

    @GetMapping("verify-reset-pass")
    public void verifyTokenAndRedirect(@RequestParam String token, HttpServletResponse response) throws IOException {
        // Redirect sang FE reset-password.html kèm token
        response.sendRedirect(baseUrl + "/auth-reset-password.html?token=" + token);
    }

    @PostMapping("reset-pass")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestParam String token,
                                                             @RequestBody Map<String, String> request) {
        String newPassword = request.get("newPassword");
        String confirmPassword = request.get("confirmPassword");

        if (token == null || newPassword == null || confirmPassword == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing required fields"));
        }
        return ResponseEntity.ok(authService.resetPassword(token, newPassword, confirmPassword));
    }
}
