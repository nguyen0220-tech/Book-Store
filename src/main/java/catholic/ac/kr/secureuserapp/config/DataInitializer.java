//package catholic.ac.kr.secureuserapp.config;
//
//import catholic.ac.kr.secureuserapp.model.entity.Role;
//import catholic.ac.kr.secureuserapp.repository.RoleRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
////class Khởi tạo ROLE_USER và ROLE_ADMIN khi LẦN ĐẦU chạy ứng dụng
//@Configuration
//@RequiredArgsConstructor
//public class DataInitializer {
//
//    private final RoleRepository roleRepository;
//
//    @Bean
//    CommandLineRunner initRoles(){
//        return args -> {
//            if(roleRepository.findByName("ROLE_USER").isEmpty()){
//                roleRepository.save(Role.builder().name("ROLE_USER").build());
//            }
//            if (roleRepository.findByName("ROLE_ADMIN").isEmpty()){
//                roleRepository.save(Role.builder().name("ROLE_ADMIN").build());
//            }
//        };
//    }
//}
