package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    // Gọi khi Spring Security cần lấy thông tin user theo username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng: " + username));

    // chuyển Set<Role> thành mảng String[] chứa tên các role (đã bỏ "ROLE_" phía trước)
        String [] roleNames=user.getRoles().stream()
                .map(role->role.getName() // trả về tên role, ví dụ "ROLE_USER"
                        .replace("ROLE_","")) //xóa "ROLE_" → còn lại "USER"
                .toArray(String[]::new); //Chuyển Stream<String> thành mảng String[]
        
        return org.springframework.security.core.userdetails.User // Dùng UserDetails có sẵn
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roleNames)
                .build();
    }

}
