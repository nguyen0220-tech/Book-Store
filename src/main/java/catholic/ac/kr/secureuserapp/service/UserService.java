package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.config.JwtUtil;
import catholic.ac.kr.secureuserapp.model.dto.LoginRequest;
import catholic.ac.kr.secureuserapp.model.dto.SignupRequest;
import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.Builder;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Builder
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ModelMapper modelMapper;

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
        return userRepository.save(user);
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
        return ResponseEntity.ok("Người dùng đã đăng ký thành công");
    }

    public ResponseEntity<?> login(LoginRequest request) {
        System.out.println("Encoded Password: " + passwordEncoder.encode(request.getPassword()));
        // Tìm người dùng theo username
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);

        // Nếu không tìm thấy user hoặc mật khẩu sai
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {           //hàm dùng để so sánh mật khẩu người dùng nhập vào (raw password)
            return ResponseEntity.badRequest().body("Tài khoản hoặc mật khẩu không đúng");              //với mật khẩu đã được mã hóa lưu trong database (encoded password)
        }                                                                                               //đến từ interface PasswordEncoder trong Spring Security.
        // Nếu đăng nhập thành công → tạo JWT
        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(token);
    }
}
