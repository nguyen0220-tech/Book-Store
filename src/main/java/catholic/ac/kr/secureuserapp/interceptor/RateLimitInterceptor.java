package catholic.ac.kr.secureuserapp.interceptor;

import catholic.ac.kr.secureuserapp.security.JwtUtil;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor { // Interface của Spring MVC cho phép chặn các request trước khi chúng vào controller (preHandle)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>(); // Map để lưu Bucket (bộ đếm yêu cầu)
    final JwtUtil jwtUtil;

    private Bucket createNewBucket() {
        // Tối đa 5 requests mỗi phút
        Refill refill = Refill.greedy(5, Duration.ofMinutes(1));
        Bandwidth limit = Bandwidth.classic(5, refill); //Bucket chứa tối đa 5 token.
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws IOException {
//        limit sử dụng IP
//        String ipAddress = request.getRemoteAddr(); //Lấy địa chỉ IP của client.
//
//        Bucket bucket=buckets.computeIfAbsent(ipAddress, k -> createNewBucket()); //Nếu IP chưa có Bucket thì tạo mới.
//
//        if (bucket.tryConsume(1)) {
//            return true;
//        }else {
//            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
//            response.getWriter().write("Too many requests - try again later.");
//            return false;
//        }

//        limit sử dụng Jwt
        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Missing or invalid token.");
            return false;
        }

        String pureToken = token.substring(7);

        String username;
        try {
            username = jwtUtil.extractUsername(pureToken);
        } catch (Exception e) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write("Token has expired.");
            return false;
        }

        Bucket bucket = buckets.computeIfAbsent(username, k -> createNewBucket()); //computeIfAbsent() sẽ tạo bucket mới nếu chưa có

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests.");
            return false;
        }
    }
}
