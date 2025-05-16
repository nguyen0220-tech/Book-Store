package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.mapper.UserMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.VerificationTokenRepository;
import catholic.ac.kr.secureuserapp.security.JwtUtil;
import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.model.entity.VerificationToken;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.security.token.TokenService;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final Map<String, Integer> loginFailCounts = new ConcurrentHashMap<>();

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<List<UserDTO>>> findAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOs = userMapper.toDTO(users); // dùng MapStruct

        ApiResponse<List<UserDTO>> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);

        apiResponse.setMessage("Lấy danh sách người dùng thành công");
        apiResponse.setData(userDTOs);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> findAllUsersByNamePaging(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchByName(keyword, pageable);
        Page<UserDTO> result = userMapper.toDTO(users); // dùng MapStruct

        ApiResponse<Page<UserDTO>> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Kết quả tìm kiếm người dùng theo tên");
        apiResponse.setData(result);
        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ResponseEntity<ApiResponse<Page<UserDTO>>> findAllUsersByRole(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByRole(role, pageable);
        Page<UserDTO> userDTOS = userMapper.toDTO(users); // dùng MapStruct

        ApiResponse<Page<UserDTO>> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Kết quả lọc người dùng theo giới tính");
        apiResponse.setData(userDTOS);
        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> saveUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            ApiResponse<User> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("Người dùng đã tồn tại");

            return ResponseEntity.ok(apiResponse);
        }

        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .enabled(true) //do admin có quyền thêm vào nên khi thêm tài khoản sẽ được kích hoạt luôn
                .build();

        User savedUser = userRepository.save(newUser); //lưu lại vào DB

        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Đã thêm người dùng thành công");
        apiResponse.setData(savedUser);

        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(Long id, UserDTO userDTO) {
        Optional<User> userOptional = userRepository.findById(id); // Tìm user theo id trong DB

        if (userOptional.isEmpty()) {
            ApiResponse<UserDTO> apiResponse = new ApiResponse<>();
            apiResponse.setSuccess(false);
            apiResponse.setMessage("User not found");

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponse);
        }

        User existingUser = userOptional.get();
        existingUser.setUsername(userDTO.getUsername());

//        xu li Set<RoleDTO>
        Set<Role> roles = userDTO.getRoles().stream() // Lấy danh sách RoleDTO từ UserDTO → biến nó thành Stream
                .map(roleDTO -> roleRepository.findByName(roleDTO.getName()) //Dùng để biến đổi mỗi phần tử RoleDTO thành Role thật sự từ database
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleDTO.getName())))
                .collect(Collectors.toSet()); //hu thập tất cả các Role từ stream và gom lại thành một Set<Role>

        existingUser.setRoles(roles);

        User updatedUser = userRepository.save(existingUser); // Lưu user đã cập nhật vào database

        UserDTO updatedUserDTO = userMapper.toDTO(updatedUser); // Chuyển đổi entity sang DTO để trả về client

        ApiResponse<UserDTO> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Cập nhật user thành công");
        apiResponse.setData(updatedUserDTO);

        return ResponseEntity.ok(apiResponse);
    }

    @Transactional
    //là một cơ chế giúp quản lý giao dịch (transaction) của database một cách tự động, giúp đảm bảo tính toàn vẹn dữ liệu và hỗ trợ rollback khi có lỗi xảy ra.
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.deleteById(user.getId());

        ApiResponse<User> apiResponse = new ApiResponse<>();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Đã xóa người dùng thành công ");
        apiResponse.setData(user);
        return ResponseEntity.ok(apiResponse);
    }

    public ResponseEntity<?> signUp(SignupRequest request) {
        System.out.println("Username: " + request.getUsername()); //in ra console username
        System.out.println("Password: " + request.getPassword()); //in ra console PW
        System.out.println("Encoded Password: " + passwordEncoder.encode(request.getPassword())); //in ra PW đã mã hóa

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên người dùng đã được lấy");
        }

        // Tìm role USER từ DB
        Role defaultRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ROLE_USER trong DB"));

        // Tạo user mới với password đã mã hóa
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(Set.of(defaultRole))
                .build();
        userRepository.save(user);

        tokenService.sendUnlockEmail(user);

        return ResponseEntity.ok("Đã gửi email xác nhận");
    }

    public ResponseEntity<?> verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null || verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn");
        }
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);//sau khi xac thuc token thi xoa

        return ResponseEntity.ok("Tài khoản đã được xác thực thành công!");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        String username = request.getUsername();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Tài khoản không tồn tại");
        }
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());

            log.info("{}: Đăng nhập thành công.", request.getUsername());
            loginFailCounts.remove(username); // Nếu login thành công → reset số lần nhập sai
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            if (e instanceof DisabledException || (e.getCause() instanceof DisabledException)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Tài khoản chưa được xác thực qua email.");
            } else if (e instanceof BadCredentialsException || (e.getCause() instanceof BadCredentialsException)) {
                int count = loginFailCounts.getOrDefault(username, 0) + 1;
                loginFailCounts.put(username, count);
                log.info("{} Nhập sai mật khẩu lần {}", username, count);

                if (count >= 5) {
                    log.warn("Tài khoản {} đã nhập sai mật khẩu quá 5 lần. Khóa tạm thời!", username); //log ra file log

                    user.setEnabled(false);
                    userRepository.save(user);

                    tokenService.sendUnlockEmail(user);

                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body("Bạn đã nhập sai mật khẩu quá 5 lần. Tài khoản tạm bị khóa,vui lòng xãc thực qua email."); //hiển thị với người dùng

                }
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Tài khoản hoặc mật khẩu không đúng (" + count + "/5)");
            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Có lỗi xảy ra khi đăng nhập.");
            }
        }
    }
}
