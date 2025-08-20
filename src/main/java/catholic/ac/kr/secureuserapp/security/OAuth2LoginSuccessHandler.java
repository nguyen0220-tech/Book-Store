package catholic.ac.kr.secureuserapp.security;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.TokenResponse;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.Role;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RoleRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.service.RefreshTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public OAuth2LoginSuccessHandler(final JwtUtil jwtUtil,
                                     final UserRepository userRepository,
                                     final RefreshTokenService refreshTokenService,
                                     final RoleRepository roleRepository,
                                     final ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.refreshTokenService = refreshTokenService;
        this.roleRepository = roleRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();


        String name = oAuth2User.getAttribute("name");
        String email = oAuth2User.getAttribute("email");
        String username = oAuth2User.getAttribute("login");// GitHub dùng "login"

        if (username == null) {
            username = email;
        }

        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Role not found"));

        String finalUsername = username;
        User user = userRepository.findByUsername(username)
                .orElseGet(() -> {
                    User u = new User();
                    u.setUsername(finalUsername);
                    u.setPassword(UUID.randomUUID().toString());
                    u.setFullName(name);
                    u.setEnabled(true);
                    u.setRoles(Set.of(role));
                    return userRepository.save(u);
                });


        Map<String, Object> claims = new HashMap<>();
        claims.put("id", user.getId());
        claims.put("username", user.getUsername());
        claims.put("role", user.getRoles().stream().map(Role::getName).toList());

        String accessToken = jwtUtil.generateAccessToken(user.getUsername(), claims);

        String userAgent = request.getHeader("User-Agent");
        String ipAddress = request.getRemoteAddr();
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(
                user,
                UUID.randomUUID().toString(),
                userAgent,
                ipAddress).getData();

        // Redirect về SPA kèm token
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        TokenResponse tokenResponse = new TokenResponse(accessToken, refreshToken.getToken(), user.getId());

        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setSuccess(true);
        apiResponse.setMessage("Successfully logged in");
        apiResponse.setData(tokenResponse);

        response.setContentType("text/html");
        response.getWriter().write("<script>" +
                "window.opener.postMessage(" + objectMapper.writeValueAsString(apiResponse) + ", window.origin);" +
                "window.close();" +
                "</script>");

    }
}
