package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
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
    public ApiResponse<List<UserDTO>> findAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOs = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found",userDTOs);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<Page<UserDTO>> findAllUsersByNamePaging(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchByName(keyword, pageable);
        Page<UserDTO> result = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found with name: " + keyword,result);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<Page<UserDTO>> findAllUsersByRole(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByRole(role, pageable);
        Page<UserDTO> userDTOS = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found with role " + role,userDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Object> saveUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
//        if (existingUser.isPresent()) {
//            return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.error(" already exists"));
//        }

        if (existingUser.isPresent()) {
            return ApiResponse.error(" already exists");
        }

        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .enabled(true) //do admin có quyền thêm vào nên khi thêm tài khoản sẽ được kích hoạt luôn
                .build();

        User savedUser = userRepository.save(newUser); //lưu lại vào DB

        return ApiResponse.success("User created successfully",savedUser);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new  ResourceNotFoundException("User not found")); // Tìm user theo id trong DB


        user.setUsername(userDTO.getUsername());

//        xu li Set<RoleDTO>
        Set<Role> roles = userDTO.getRoles().stream() // Lấy danh sách RoleDTO từ UserDTO → biến nó thành Stream
                .map(roleDTO -> roleRepository.findByName(roleDTO.getName()) //Dùng để biến đổi mỗi phần tử RoleDTO thành Role thật sự từ database
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleDTO.getName())))
                .collect(Collectors.toSet()); //hu thập tất cả các Role từ stream và gom lại thành một Set<Role>

        user.setRoles(roles);

        User updatedUser = userRepository.save(user); // Lưu user đã cập nhật vào database

        UserDTO updatedUserDTO = userMapper.toDTO(updatedUser); // Chuyển đổi entity sang DTO để trả về client

        return ApiResponse.success("User updated successfully",updatedUserDTO);
    }

    //@Transactional là một cơ chế giúp quản lý giao dịch (transaction) của database một cách tự động, giúp đảm bảo tính toàn vẹn dữ liệu và hỗ trợ rollback khi có lỗi xảy ra.
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<User> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(()-> new  ResourceNotFoundException("User not found"));

        userRepository.deleteById(user.getId());

        return ApiResponse.success("User deleted successfully",user);
    }

    public ApiResponse<UserDTO> signUp(SignupRequest request) {
        log.info("Username: {} ", request.getUsername());
        log.info("Encoded Password: {}", passwordEncoder.encode(request.getPassword()));

        Optional<User> userOptional = userRepository.findByUsername(request.getUsername());
        if (userOptional.isPresent()) {
            return ApiResponse.error(request.getUsername()+" already exists");
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

        return ApiResponse.success("Sent email to "+user.getUsername()+" successfully");
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

        return ApiResponse.success(user.getUsername()+" verified successfully");
    }

    public ApiResponse<Object> login(LoginRequest request) {
        String username = request.getUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

            // Tạo claims bổ sung (userId,username, roles, ...)
            Map<String,Object> claims = new HashMap<>();
            claims.put("id", user.getId());
            claims.put("username",user.getUsername());
            claims.put("roles",user.getRoles());
            String token = jwtUtil.generateToken(userDetails.getUsername(),claims);

            loginFailCounts.remove(username); // Nếu login thành công → reset số lần nhập sai
            return ApiResponse.success("Login successful",Map.of("token",token));
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
                return ApiResponse.error("Có lỗi xảy ra khi đăng nhập");
            }
        }
    }
}
