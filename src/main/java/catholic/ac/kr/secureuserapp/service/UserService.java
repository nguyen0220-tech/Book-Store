package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.repository.VerificationTokenRepository;
import catholic.ac.kr.secureuserapp.security.JwtUtil;
import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.model.entity.VerificationToken;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import lombok.Builder;
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
import java.util.UUID;

@Service
@Builder
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;

    public User converrToUser(UserDTO userDTO) {
        return modelMapper.map(userDTO, User.class);
    }

    public UserDTO convertToUserDTO(User user) {
        return modelMapper.map(user,UserDTO.class);
    }

    public ResponseEntity<List<UserDTO>> findAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToUserDTO)
                .toList();
        return ResponseEntity.ok(userDTOs);
    }

    public ResponseEntity<Page<UserDTO>> findAllUsersByNamePaging(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page,size);
        Page<User> users = userRepository.searchByName(keyword,pageable);
        Page<UserDTO> result = users.map(this::convertToUserDTO);
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Page<UserDTO>>  findAllUsersByRole(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page,size);
        Page<User> users=userRepository.findByRole(role,pageable);
        Page<UserDTO> userDTOS=users.map(this::convertToUserDTO);
        return ResponseEntity.ok(userDTOS);
    }

    public User saveUser(User user) {
        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole())
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
        existingUser.setRole(user.getRole());

        User updatedUser = userRepository.save(existingUser);
        return ResponseEntity.ok(updatedUser);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public ResponseEntity<?> signUp(SignupRequest request) {
        System.out.println("Username: " + request.getUsername()); //in ra console username
        System.out.println("Password: " + request.getPassword()); //in ra console PW
        System.out.println("Encoded Password: " + passwordEncoder.encode(request.getPassword())); //in ra PW đã mã hóa

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Tên người dùng đã được lấy");
        }
        // Tạo user mới với password đã mã hóa
        User user = User.builder()        //builder() là phương thức được tạo tự động bởi Lombok khi dùng annotation @Builder trong class User
                .username(request.getUsername())        //Thay vì viết: User user = new User("john", "123", "ROLE_USER");
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())  // mặc định gán quyền USER,có thể sửa giá trị
                .build();           //build() là phương thức cuối cùng trong chuỗi builder. Nó dùng để "xây dựng" ra object thật sự từ các giá trị đã truyền.
        userRepository.save(user);

        // Tạo token xác thực
        String token= UUID.randomUUID().toString();
        VerificationToken verificationToken=new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryTime(LocalDateTime.now().plusDays(1)); //hạn xác thực 1 1day

        verificationTokenRepository.save(verificationToken);

        String verifyLink="http://localhost:8080/api/verify?token="+token;

        emailService.sendSimpleMail(
                user.getUsername(),
                "Xác nhận tài khoản",
                "Click vào link để xác thực: " + verifyLink
        );

        return ResponseEntity.ok("Đã gửi email xác nhận");
    }

    public ResponseEntity<?> verifyEmail(String token) {
        VerificationToken verificationToken=verificationTokenRepository.findByToken(token).orElse(null);
        if (verificationToken == null ||verificationToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Token không hợp lệ hoặc đã hết hạn");
        }
         User user=verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);//sau khi xac thuc token thi xoa

        return ResponseEntity.ok("Tài khoản đã được xác thực thành công!");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        System.out.println("Encoded Password: " + passwordEncoder.encode(request.getPassword()));
        // Tìm người dùng theo username
        //User user = userRepository.findByUsername(request.getUsername()).orElse(null);

//        // Nếu không tìm thấy user hoặc mật khẩu sai
//        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {           //hàm dùng để so sánh mật khẩu người dùng nhập vào (raw password)
//            return ResponseEntity.badRequest().body("Tài khoản hoặc mật khẩu không đúng");              //với mật khẩu đã được mã hóa lưu trong database (encoded password)
//        }                                                                                               //đến từ interface PasswordEncoder trong Spring Security.
//        // Nếu đăng nhập thành công → tạo JWT
//        String token = jwtUtil.generateToken(user.getUsername());
//        return ResponseEntity.ok(token);
        try {
            Authentication authentication=authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            MyUserDetails userDetails=(MyUserDetails) authentication.getPrincipal();
            String token=jwtUtil.generateToken(userDetails.getUsername());
            return ResponseEntity.ok(token);
         }catch (Exception e) {
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
