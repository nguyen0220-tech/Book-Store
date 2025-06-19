package catholic.ac.kr.secureuserapp.controller;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.LogoutRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.security.JwtUtil;
import catholic.ac.kr.secureuserapp.service.auth.AuthService;
import catholic.ac.kr.secureuserapp.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;

    @PostMapping("signup")
    public ResponseEntity<ApiResponse<?>> signup(@Valid @RequestBody SignupRequest request) {
        return ResponseEntity.ok(authService.signUp(request));
    }

    @GetMapping("verify")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String token) {
        return ResponseEntity.ok(authService.verifyEmail(token));
    }

    @PostMapping("login")
    public ResponseEntity<ApiResponse<?>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestBody LogoutRequest request) {
        return ResponseEntity.ok(authService.logout(request));
    }

    @PostMapping("refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refresh_token");
        if (refreshTokenStr == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Missing refresh token"));
        }

        Optional<RefreshToken> refreshTokenOptional = refreshTokenService.findRefreshToken(refreshTokenStr);
        if (refreshTokenOptional.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid refresh token"));
        }

        RefreshToken refreshToken = refreshTokenOptional.get();
        if(!refreshTokenService.isValid(refreshToken)){
            refreshTokenService.deleteByUser(refreshToken.getUser());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Refresh token expired. Please login again."));
        }

        User user = refreshToken.getUser();
        Map<String,Object> claims = Map.of(
                "id",user.getId(),
                "username",user.getUsername(),
                "roles",user.getRoles()
        );

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);

        return ResponseEntity.ok(ApiResponse.success("Token refresh",Map.of("access_token",accessToken)));
    }
}
