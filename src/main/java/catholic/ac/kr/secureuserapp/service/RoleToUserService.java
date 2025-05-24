package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleToUserService {
    public final RoleRepository roleRepository;
    public final UserRepository userRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Set<Role>> getRoleOfUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(" User not found: " + username));

        return ApiResponse.success("Get role of user successfully", user.getRoles());
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> removeRoleFromUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.getRoles().remove(role);
        userRepository.save(user);
        return ApiResponse.success("Role removed successfully");
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> addRoleToUser(String username, String roleName) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));

        user.getRoles().add(role);
        userRepository.save(user);

        return ApiResponse.success("Role added successfully");
    }
}
