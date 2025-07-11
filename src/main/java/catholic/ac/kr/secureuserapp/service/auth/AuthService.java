package catholic.ac.kr.secureuserapp.service.auth;

import catholic.ac.kr.secureuserapp.exception.AlreadyExistsException;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.model.entity.VerificationToken;
import catholic.ac.kr.secureuserapp.repository.RefreshTokenRepository;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.repository.VerificationTokenRepository;
import catholic.ac.kr.secureuserapp.security.JwtUtil;
import catholic.ac.kr.secureuserapp.security.token.TokenService;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import catholic.ac.kr.secureuserapp.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final Map<String, Integer> loginFailCounts = new ConcurrentHashMap<>();
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;


    public ApiResponse<UserDTO> signUp(SignupRequest request) {
        log.info("Username: {} ", request.getUsername());
        log.info("Encoded Password: {}", passwordEncoder.encode(request.getPassword()));

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isPresent()) {
            throw new AlreadyExistsException("User already exists");
        }

        // Tạo ROLE_USER mặc định khi đăng kí (Tìm role USER từ DB)
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_USER trong DB"));

        // Tạo user mới với password đã mã hóa
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(defaultRole))
                .build();

        userRepository.save(user); //phải lưu vào DB trước rồi mới có thể tạo token gán vào
        tokenService.sendUnlockEmail(user);

        return ApiResponse.success("Sent email to " + user.getUsername() + " successfully");
    }

    public ApiResponse<UserDTO> verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null || verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Token already expired/Token không hợp lệ hoặc đã hết hạn");
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);//sau khi xac thuc token thi xoa

        return ApiResponse.success(user.getUsername() + " verified successfully");
    }

    public ApiResponse<TokenResponse> login(LoginRequest request, HttpServletRequest httpRequest) {
        //  Tự động lấy thông tin thiết bị
        String userAgent = httpRequest.getHeader("User-Agent");
        String ipAddress = httpRequest.getRemoteAddr();

        //  Gán vào DTO (nếu muốn tiếp tục dùng DTO hiện tại)
        request.setUserAgent(userAgent);
        request.setIpAddress(ipAddress);

        //  Tạo ngẫu nhiên deviceId nếu không truyền từ client (VD: mobile app)
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            request.setDeviceId(UUID.randomUUID().toString());
        }

        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

            // Claims to JWT (userId,username, roles, ...)
            Map<String, Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username", user.getUsername());
            claims.put("roles", user.getRoles().stream().map(Role::getName).toList());

            String accessToken = jwtUtil.generateAccessToken(userDetails.getUsername(), claims);

            RefreshToken refreshToken = refreshTokenService
                    .createRefreshToken(user, request.getDeviceId(), request.getUserAgent(), request.getIpAddress())
                    .getData();

            loginFailCounts.remove(username); // Nếu login thành công → reset số lần nhập sai

            TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken.getToken(), user.getId());

            return ApiResponse.success("Successfully logged in", tokenResponse);
        } catch (Exception e) {
            if (e instanceof DisabledException || (e.getCause() instanceof DisabledException)) {
                return ApiResponse.error("Tài khoản chưa được xác thực qua email.");

            } else if (e instanceof BadCredentialsException || (e.getCause() instanceof BadCredentialsException)) {
                int count = loginFailCounts.getOrDefault(username, 0) + 1;
                loginFailCounts.put(username, count);
                log.info("{} Nhập sai mật khẩu lần {}", username, count);

                if (count >= 5) {
                    log.warn("Tài khoản {} đã nhập sai mật khẩu quá 5 lần. Khóa tạm thời!", username); //log ra file log

                    user.setEnabled(false);
                    userRepository.save(user);

                    tokenService.sendUnlockEmail(user);

                    return ApiResponse.error("Bạn đã nhập sai mật khẩu quá 5 lần. Tài khoản tạm bị khóa,vui lòng xãc thực qua email."); //hiển thị với người dùng

                }
                return ApiResponse.error("Tài khoản hoặc mật khẩu không đúng (" + count + "/5)");
            } else {
//                e.printStackTrace();
                return ApiResponse.error("Có lỗi xảy ra khi đăng nhập");
            }
        }
    }

    @Transactional
    public ApiResponse<String> logout(LogoutRequest request) {
        int deleted = refreshTokenRepository.deleteByToken(request.getRefreshToken());

        if (deleted > 0) {
            return ApiResponse.success("Logout success");
        } else
            return ApiResponse.error("Logout failed");
    }

    public ApiResponse<TokenResponse> refresh(String refreshTokenStr) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenStr)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));

        if (!refreshTokenService.isValid(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }

//      Revoke current token
        refreshTokenService.revokeToken(refreshToken);

        // Tạo access và refresh token mới
        User user = refreshToken.getUser();
        Map<String, Object> claims = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles());

        String newAccessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(
                        user,
                        refreshToken.getDeviceId(),
                        refreshToken.getUserAgent(),
                        refreshToken.getIpAddress())
                .getData();

        TokenResponse tokenResponse = new TokenResponse(newAccessToken, newRefreshToken.getToken(), user.getId());

        return ApiResponse.success("Refreshed token", tokenResponse);
    }
}
