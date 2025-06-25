package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

//class Khởi tạo ADMIN khi LẦN ĐẦU chạy ứng dụng
@Service
@RequiredArgsConstructor
public class AddAdminService {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    //@PostConstruct: Phương thức addUserWithRoleAdmin() sẽ được gọi tự động sau khi tất cả các phụ thuộc được inject vào bean.
    public void addUserWithRoleAdmin() {
        System.out.println(" Bắt đầu kiểm tra và khởi tạo admin");

        roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> {
                    System.out.println("Tạo mới ROLE_USER");
                    return roleRepository.save(Role.builder().name("ROLE_USER").build());
                });

        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN")
                .orElseGet(() -> {
                    System.out.println("Tạo mới ROLE_ADMIN");
                    return roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
                });

        if (userRepository.findByUsername("admin").isEmpty()) {
            User user = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456"))
                    .enabled(true)
                    .roles(Set.of(roleAdmin))
                    .build();

            userRepository.save(user);
        }else
            System.out.println("admin already exist");


    }
}
