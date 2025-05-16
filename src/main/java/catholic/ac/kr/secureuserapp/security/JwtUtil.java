package catholic.ac.kr.secureuserapp.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

//=========Sinh và xác minh JWT============
@Component // Đánh dấu class này là một bean của Spring
public class JwtUtil {
    // Tạo khóa bí mật để ký JWT, dùng thuật toán HMAC-SHA256
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // Thời gian hết hạn của token là 1 giờ (tính bằng mili-giây)
    private final long EXPIRATION_TIME = 1000 * 60 * 60;
    //  Hàm này dùng để tạo token JWT từ username khi khi đăng nhập thành công
    public String generateToken(String username) {
        return Jwts.builder() //Bắt đầu tạo JWT
                .setSubject(username) // Đặt thông tin chính là username
                .setIssuedAt(new Date()) // Ngày phát hành
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key)  // Ký token bằng khóa bí mật
                .compact(); //Kết thúc và trả về chuỗi JWT
    }

    // Hàm này giải mã JWT và lấy ra username
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // Cung cấp key để giải mã
                .build()
                .parseClaimsJws(token) // Phân tích token
                .getBody()
                .getSubject(); // Lấy subject (username)
    }


    //  Kiểm tra token có hợp lệ hay không
    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
