package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @GetMapping("verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
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
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(e.getMessage()));
        }
    }
}
