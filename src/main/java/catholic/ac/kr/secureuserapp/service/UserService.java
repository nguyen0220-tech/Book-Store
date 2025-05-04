package catholic.ac.kr.secureuserapp.service;

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
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user, UserDTO.class);
    }

    public ResponseEntity<List<UserDTO>> findAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToUserDTO)
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<Page<UserDTO>> findAllUsersByNamePaging(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.searchByName(keyword, pageable);
        Page<UserDTO> result = users.map(this::convertToUserDTO);
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Page<UserDTO>> findAllUsersByRole(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByRole(role, pageable);
        Page<UserDTO> userDTOS = users.map(this::convertToUserDTO);
        return ResponseEntity.ok(userDTOS);
    }

    public User saveUser(User user) {
        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .roles(user.getRoles()) // Gán trực tiếp tập quyền đã chọn
                .build();
        return userRepository.save(newUser);
    }

    public ResponseEntity<?> updateUser(Long id, User user) {
        Optional<User> userOptional = userRepository.findById(id);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        User existingUser = userOptional.get();
        existingUser.setUsername(user.getUsername());
        existingUser.setRoles(user.getRoles());

        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    //    CRUD quyền (role) cho một người dùng
    public ResponseEntity<?> getRoleOfUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User not found"));
        return ResponseEntity.ok(user.getRoles());

    }

    public void removeRoleFromUser(Long userId, Long roleId) {
        User user=userRepository.findById(userId).
                orElseThrow(() -> new RuntimeException("User not found"));


        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.getRoles().remove(role);

        userRepository.save(user);
    }

    public void addRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(" User not found " + username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException(" Role not found " + roleName));

        user.getRoles().add(role);

        userRepository.save(user);
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

        // Tạo token xác thực
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(LocalDateTime.now().plusDays(1)); //hạn xác thực 1 1day

        verificationTokenRepository.save(verificationToken);

        String verifyLink = "http://localhost:8080/api/verify?token=" + token;

        emailService.sendSimpleMail(
                user.getUsername(),
                "Xác nhận tài khoản",
                "Click vào link để xác thực: " + verifyLink
        );

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
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
        } catch (Exception e) {
            if (e instanceof DisabledException || (e.getCause() instanceof DisabledException)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Tài khoản chưa được xác thực qua email.");
            } else if (e instanceof BadCredentialsException || (e.getCause() instanceof BadCredentialsException)) {
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Tài khoản hoặc mật khẩu không đúng");
            } else {
                return ResponseEntity
                        .status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Có lỗi xảy ra khi đăng nhập.");
            }
        }
    }
}
