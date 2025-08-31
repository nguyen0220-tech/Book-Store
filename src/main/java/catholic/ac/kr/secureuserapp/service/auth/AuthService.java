package catholic.ac.kr.secureuserapp.service.auth;

import catholic.ac.kr.secureuserapp.Status.NotificationType;
import catholic.ac.kr.secureuserapp.exception.AlreadyExistsException;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.UserMapper;
import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.*;
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

import java.sql.Timestamp;
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
    private final UserMapper userMapper;
    private final CouponRepository couponRepository;
    private final NotificationRepository notificationRepository;


    public ApiResponse<UserDTO> signUp(SignupRequest request) {
        log.info("Username: {} ", request.getUsername());
        log.info("Encoded Password: {}", passwordEncoder.encode(request.getPassword()));

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isPresent()) {
            throw new AlreadyExistsException("User already exists");
        }

        boolean phoneExists = userRepository.existsByPhone(request.getPhone());

        if (phoneExists) {
            throw new AlreadyExistsException("Phone already exists");
        }

        // Tạo ROLE_USER mặc định khi đăng kí (Tìm role USER từ DB)
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_USER trong DB"));

        // Tạo user mới với password đã mã hóa
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .roles(Set.of(defaultRole))
                .phone(request.getPhone())
                .yearOfBirth(request.getYearOfBirth())
                .monthOfBirth(request.getMonthOfBirth())
                .dayOfBirth(request.getDayOfBirth())
                .build();

        userRepository.save(user); //phải lưu vào DB trước rồi mới có thể tạo token gán vào
        tokenService.sendUnlockEmail(user);

        return ApiResponse.success("Sent email to " + user.getUsername() + " successfully");
    }

    public boolean verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null || verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return false;
        }

        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        Coupon coupon = couponRepository.findByCouponCode("WC_STORE")
                .orElse(null);
        if (coupon != null) {
            coupon.getUsers().add(user);
            couponRepository.save(coupon);

            //chua fix duoc loi khi nguoi dung khac tao thi nguoi  dung truoc se tu dong bi ghi de -> mat coupon
            Notification notification = Notification.builder()
                    .user(user)
                    .book(null)
                    .order(null)
                    .message("Chào mừng "+user.getFullName()+" đến với Book Store. " +
                            "Chúng tôi gửi bạn Coupon khi lần đầu mua sắm tại Book Store. " +
                            "Để kiểm tra hãy truy cập: Trang cá nhân -> Coupon " +
                            "Thank you.")
                    .read(false)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .type(NotificationType.COUPON)
                    .build();

            notificationRepository.save(notification);
        }

        return true;
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

    public ApiResponse<UserDTO> findUserNameByPhone(String phoneNumber) {
        User user = userRepository.findByPhone(phoneNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng hoặc tài khoản chưa được liên kết với số điện thoại "));

        UserDTO userDTO = userMapper.toDTO(user);

        userDTO.setUsername(maskEmail(user.getUsername()));

        return ApiResponse.success("Đã tìm thấy tài khoản", userDTO);
    }

    public ApiResponse<String> forgetPassword(String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return ApiResponse.success("Nếu tài khoản tồn tại, hệ thống sẽ gửi email khôi phục.");
        }

        User user = optionalUser.get();

        // Tìm token
        Optional<VerificationToken> tokenOpt = verificationTokenRepository.findByUser(user);

        // Nếu token hết hạn → xóa
        tokenOpt.ifPresent(t -> {
            if (t.getExpiryTime().isBefore(LocalDateTime.now())) {
                verificationTokenRepository.delete(t);
            }
        });

        // Kiểm tra lại token còn tồn tại không
        boolean tokenExists = verificationTokenRepository.existsByUser(user);
        if (tokenExists) {
            return ApiResponse.success("Đã gửi link cập nhật mật khẩu tới email, vui lòng kiểm tra lại email.");
        }

        // Gửi mail tạo token mới
        tokenService.sendResetPassword(user);

        String maskEmail = maskEmail(user.getUsername());

        return ApiResponse.success("Đã gửi link cập nhật tới email. Vui lòng kiểm tra: " + maskEmail);
    }


    private String maskEmail(String email) {
        int atIndex = email.indexOf("@");

        if (atIndex <= 2)
            return "***@" + email.substring(atIndex);

        String prefix = email.substring(0, 2);
        String domain = email.substring(atIndex + 1);
        String maskDomain = domain.substring(0, 2) + "***";

        return prefix + "***@" + maskDomain;
    }

    public ApiResponse<String> resetPassword(String token,
                                             String newPassword,
                                             String confirmPassword) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null || verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ApiResponse.error("Token already expired/Token không hợp lệ hoặc đã hết hạn");
        }

        if (!newPassword.equals(confirmPassword)) {
            return ApiResponse.error("Nhập mật khẩu xác nhận không khớp");
        }

        User user = verificationToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);

        return ApiResponse.success("Đã thay đổi mật khẩu thành công");
    }

}
