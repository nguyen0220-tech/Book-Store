package catholic.ac.kr.secureuserapp.security;

import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableMethodSecurity //kích hoạt cơ chế kiểm tra phân quyền ngay tại method
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserRepository userRepository;
    private final OAuth2LoginSuccessHandler loginSuccessHandler;

    //1. Bean mã hóa mật khẩu dùng cho toàn app
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy tài khoản" + username));
            if (!user.isEnabled()) {
                throw new DisabledException("Tài khoản chưa được xác thực qua email");
            }
            return new MyUserDetails(user);
        };
    }

    @Bean
//  2. Provider dùng để xác thực user bằng username + password
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());// tìm user trong DB
        provider.setPasswordEncoder(passwordEncoder()); // mã hóa/match password
        return provider;
    }

    //  3. Cung cấp AuthenticationManager (Spring cần để xử lý login)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
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
                .csrf(csrt -> csrt.disable())                      // Tắt CSRF (thường dùng với form login, ta dùng API nên tắt)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))             // Không dùng session (vì dùng JWT)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "public",
                                "/user/find-pass",
                                "/auth/**",           // cho phép toàn bộ API auth
                                "/user/find-username",
                                "/*.html",            // tất cả file .html trong static/
                                "/*.css",             // css
                                "/*.js",              // js
                                "/*.png", "/*.jpg", "/*.svg",  // ảnh
                                "/favicon.ico",
                                "/icon/**",
                                "/ws/**"
                        )
                        .permitAll()         // cho phép không login/signup/verify...
                        .requestMatchers("/chat/**").authenticated()
                        .anyRequest().authenticated()  // các request còn lại cần token JWT hợp lệ
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jsonAuthEntryPoint()))
                .oauth2Login(oAuth2Login -> {
                    oAuth2Login.successHandler(loginSuccessHandler);
                })
                .authenticationProvider(authenticationProvider())                            // cung cấp cách xác thực người dùng
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)  // Thêm filter kiểm tra JWT trước khi đến filter mặc định
                .build();
    }

    /*
    Bean này sẽ được Spring Security dùng làm AuthenticationEntryPoint, tức là “cửa ra” khi authentication thất bại (ví dụ token hết hạn hoặc không gửi token).
    AuthenticationEntryPoint là interface của Spring Security, có nhiệm vụ xử lý khi người dùng chưa xác thực (unauthenticated) cố truy cập vào resource cần login
     */
    @Bean
    public AuthenticationEntryPoint jsonAuthEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(
                    Map.of("success", false, "message", "Unauthorized")
            ));
        };
    }
}
