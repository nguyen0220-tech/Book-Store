package catholic.ac.kr.secureuserapp.security;

import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import catholic.ac.kr.secureuserapp.security.userdetails.MyUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component // Cho phép Spring quản lý filter này
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { //filter đặc biệt của Spring – đảm bảo chỉ chạy 1 lần cho mỗi request.
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
//     Mỗi lần có request đến, hàm này sẽ chạy để kiểm tra JWT
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
//      1. Lấy giá trị từ header "Authorization"
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; //Không có header Authorization Hoặc không bắt đầu bằng "Bearer "→ Bỏ qua filter này, để tiếp tục xử lý bởi các filter khác
        }

//      2. Lấy token JWT từ header (bỏ chữ "Bearer " phía trước)
        String token = authHeader.substring(7);

//      3. Kiểm tra token có hợp lệ không
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return; //Nếu không hợp lệ (hết hạn, sai chữ ký, v.v.), thì bỏ qua request
        }
//      4. Lấy username từ token
        String username = jwtUtil.extractUsername(token);

//      5. Kiểm tra nếu user chưa được xác thực
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
//             Dùng UserRepository để lấy user từ database dựa trên username trong JWT
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
//              6. Tạo Authentication object với username và quyền (roles)
//                UserDetails: là interface bắt buộc cho Spring Security để xử lý xác thực
//                UserDetails userDetails = org.springframework.security.core.userdetails.User
//                        .withUsername(user.getUsername())
//                        .password(user.getPassword()) //Dù không cần mật khẩu ở đây (vì đã login rồi), vẫn phải truyền vào password
//                        .authorities(
//                                user.getRoles().stream()  //trả về Set<Role>
//                                        .map(Role::getName) // Lấy tên từng role: "ROLE_USER", "ROLE_ADMIN"
//                                        .toArray(String[]::new)) //chuyển sang mảng String[] đúng format mà cần
//                        .build();

//                6.1 set MyUserDetails vào SecurityContext
                UserDetails userDetails = new MyUserDetails(user);

//                Đây là đối tượng xác thực cho Spring Security hiểu rằng: "Người này đã đăng nhập"
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,        // thông tin người dùng
                                null,               // mật khẩu không cần (vì xác thực qua JWT)
                                userDetails.getAuthorities()  // danh sách role như: ROLE_USER
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); //Gắn các chi tiết ( như địa chỉ IP, session ID...) và lưu vào context

//              7.   Lưu thông tin xác thực vào SecurityContext → cho phép request đi tiếp
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
//      8. Tiếp tục chạy các filter tiếp theo
        filterChain.doFilter(request, response);
    }

    @Override
    protected  boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        return path.equals("/auth/refresh");

    }
}

/*
Client gửi request có JWT
↓
JwtAuthenticationFilter:
- lấy token từ header
- kiểm tra token hợp lệ → isTokenValid()
- lấy username → extractUsername()
- tạo Authentication và gán vào context
↓
Spring Security cho phép truy cập API

 */