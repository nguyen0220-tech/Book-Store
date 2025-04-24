package catholic.ac.kr.secureuserapp.config;

import catholic.ac.kr.secureuserapp.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity //kích hoạt cơ chế kiểm tra phân quyền ngay tại method
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService; // Spring dùng để tìm user theo username

    //1. Bean mã hóa mật khẩu dùng cho toàn app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
//  2. Provider dùng để xác thực user bằng username + password
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider=new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);// tìm user trong DB
        provider.setPasswordEncoder(passwordEncoder()); // mã hóa/match password
        return provider;
    }
//  3. Cung cấp AuthenticationManager (Spring cần để xử lý login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception{
        return config.getAuthenticationManager();
    }

//  4. Cấu hình filter chain chính
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())  //Tạm thời tắt yêu cầu đăng nhập để test API
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll()); //Cho phép mọi request mà KHÔNG yêu cầu xác thực
//        return http.build();

        return http
                .csrf( csrt -> csrt.disable())                      // Tắt CSRF (thường dùng với form login, ta dùng API nên tắt)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))             // Không dùng session (vì dùng JWT)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/signup","/api/login").permitAll()         // cho phép không login/signup
                        .anyRequest().authenticated()                                        // các request còn lại cần token JWT hợp lệ
                )
                .authenticationProvider(authenticationProvider())                            // cung cấp cách xác thực người dùng
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)  // Thêm filter kiểm tra JWT trước khi đến filter mặc định
                .build();
    }


}
