//package catholic.ac.kr.secureuserapp.service;
//
//import catholic.ac.kr.secureuserapp.model.entity.Role;
//import catholic.ac.kr.secureuserapp.model.entity.User;
//import catholic.ac.kr.secureuserapp.repository.RoleRepository;
//import catholic.ac.kr.secureuserapp.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//
//import java.util.Set;
//
////class Khởi tạo ADMIN khi LẦN ĐẦU chạy ứng dụng
//@Service
//public class AddAdminService {
//    @Autowired
//    private RoleRepository roleRepository;
//    @Autowired
//    private UserRepository userRepository;
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @PostConstruct
//    //@PostConstruct: Phương thức addUserWithRoleAdmin() sẽ được gọi tự động sau khi tất cả các phụ thuộc được inject vào bean.
//    public void addUserWithRoleAdmin() {
//        User user = new User();
//
//        user.setUsername("admin"); //dat ten admin
//        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
//            return;
//        }
//        user.setPassword(passwordEncoder.encode("123456")); //mac dinh 123456
//        user.setEnabled(true); //mac dinh kich hoat
//
//        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElseThrow(() -> new RuntimeException("Role not found"));
//
//        user.setRoles(Set.of(roleAdmin));
//
//        userRepository.save(user);
//    }
//
//}
