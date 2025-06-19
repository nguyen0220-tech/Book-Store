package catholic.ac.kr.secureuserapp.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

//=========Sinh và xác minh JWT============
@Component // Đánh dấu class này là một bean của Spring
public class JwtUtil {
    @Value("${jwt.secret}") //Spring sẽ inject giá trị từ application.properties
    private String secret;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); //Tạo đối tượng Key từ chuỗi bí mật -> Biến chuỗi thành mảng byte
    }

    public String generateAccessToken (String username, Map<String, Object> claims) {
        return generateToken(username,claims,Duration.ofMinutes(15));
    }

    //  Hàm này dùng để tạo token JWT từ username khi khi đăng nhập thành công
    public String generateToken(String username, Map<String, Object> claims,Duration expiration) {
        // Thời gian hết hạn của token là 1 giờ (tính bằng mili-giây)
        return Jwts.builder() //Bắt đầu tạo JWT
                .setClaims(claims) //Thêm claims tùy chọn
                .setSubject(username) // Đặt thông tin chính là username
                .setIssuedAt(new Date()) // Ngày phát hành
                .setExpiration(new Date(System.currentTimeMillis() + expiration.toMillis()))
                .signWith(getSigningKey())  // Ký token bằng khóa bí mật
                .compact(); //Kết thúc và trả về chuỗi JWT
    }

    // Hàm này giải mã JWT và lấy ra username
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Cung cấp key để giải mã
                .build()
                .parseClaimsJws(token) // Phân tích token
                .getBody()
                .getSubject(); // Lấy subject (username)
    }

    //  Kiểm tra token có hợp lệ hay không
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
