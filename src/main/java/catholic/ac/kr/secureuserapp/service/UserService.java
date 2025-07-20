package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.exception.AlreadyExistsException;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.UserMapper;
import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    @Cacheable(value = "userCache", key = "#userId")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> findUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ResourceNotFoundException("User with id " + userId + " not found"));
        UserDTO userDTO = userMapper.toDTO(user);

        return ApiResponse.success("User found with id " + user.getId(), userDTO);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<UserDTO>> findAllUsers() {
        List<User> users = userRepository.findAll();

        List<UserDTO> userDTOs = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found", userDTOs);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserDTO>> findAllUsersByNamePaging(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("username").ascending());
        Page<User> users = userRepository.searchByName(keyword, pageable);
        Page<UserDTO> result = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found with name: " + keyword, result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<UserDTO>> findAllUsersByRole(int page, int size, String role) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userRepository.findByRole(role, pageable);
        Page<UserDTO> userDTOS = userMapper.toDTO(users); // dùng MapStruct

        return ApiResponse.success("Users found with role: " + role, userDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new AlreadyExistsException(user.getUsername() + " already exists");
        }

        User newUser = User.builder()
                .username(user.getUsername())
                .password(passwordEncoder.encode(user.getPassword()))
                .enabled(true) //do admin có quyền thêm vào nên khi thêm tài khoản sẽ được kích hoạt luôn
                .build();

        userRepository.save(newUser); //lưu lại vào DB

        return ApiResponse.success("User created successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found")); // Tìm user theo id trong DB

        if (!user.getUsername().equals(userDTO.getUsername())) {
            if (userRepository.findByUsername(userDTO.getUsername()).isPresent()) {
                throw new AlreadyExistsException(userDTO.getUsername() + " already exists");
            }
            user.setUsername(userDTO.getUsername());
        }

//        xu li Set<RoleDTO>
        Set<Role> roles = userDTO.getRoles().stream() // Lấy danh sách RoleDTO từ UserDTO → biến nó thành Stream
                .map(roleDTO -> roleRepository.findByName(roleDTO.getName()) //Dùng để biến đổi mỗi phần tử RoleDTO thành Role thật sự từ database
                        .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleDTO.getName())))
                .collect(Collectors.toSet()); //hu thập tất cả các Role từ stream và gom lại thành một Set<Role>

        user.setRoles(roles);

        User updatedUser = userRepository.save(user); // Lưu user đã cập nhật vào database

        UserDTO updatedUserDTO = userMapper.toDTO(updatedUser); // Chuyển đổi entity sang DTO để trả về client

        return ApiResponse.success("User updated successfully", updatedUserDTO);
    }

    //@Transactional là một cơ chế giúp quản lý giao dịch (transaction) của database một cách tự động, giúp đảm bảo tính toàn vẹn dữ liệu và hỗ trợ rollback khi có lỗi xảy ra.
    @Transactional
    @CacheEvict(value = "userCache", key = "#id") //Xoá cache khi xoá hoặc cập nhật
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<UserDTO> deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        userRepository.deleteById(user.getId());

        return ApiResponse.success("User deleted successfully");
    }

    public ApiResponse<UserProfileDTO> getUserProfile(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        UserProfileDTO userProfileDTO = userMapper.toUserProfileDTO(user);

        return ApiResponse.success("User profile", userProfileDTO);
    }

    public ApiResponse<UserProfileDTO> updateUserProfile(Long id, UpdateProfileRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user.setFullName(request.getFullName());
        user.setLiking(request.getLiking());
        user.setAddress(request.getAddress());
        user.setPhone(request.getPhone());
        user.setSex(request.getSex());

        userRepository.save(user);

        UserProfileDTO userProfileDTO = userMapper.toUserProfileDTO(user);

        return ApiResponse.success("Updated user profile", userProfileDTO);
    }

    @Transactional
    public ApiResponse<String> changePassword(Long id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error("Old password does not match");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ApiResponse.error("Password does not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        emailService.sendSimpleMail(
                user.getUsername(),
                "Password changed",
                "Your password has been changed"
        );

        return ApiResponse.success("Password changed successfully");
    }
}
