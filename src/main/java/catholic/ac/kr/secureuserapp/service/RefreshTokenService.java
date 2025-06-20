package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public ApiResponse<RefreshToken> createRefreshToken(User user) {
//        refreshTokenRepository.deleteByUser(user); // nếu dung đa thiết bi thì không đuoc xóa
//        refreshTokenRepository.flush(); //phương thức của EntityManager (và JpaRepository) để ép JPA đồng bộ dữ liệu từ bộ nhớ tạm (persistence context) xuống database ngay lập tức

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(3));

        refreshTokenRepository.save(token);

        return ApiResponse.success("Refresh token created",token);
    }

    public boolean isValid(RefreshToken token) {
        return token!= null && !token.isRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    public Optional<RefreshToken> findRefreshToken(String token) {
        return  refreshTokenRepository.findByToken(token);
    }

    // Revoke token (thu hồi) khi đã sử dụng xong
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    @Transactional
    public boolean deleteByToken(String token) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(token);
        if (refreshToken.isPresent()) {
            refreshTokenRepository.delete(refreshToken.get());
            return true;
        }
        return false;
    }
}
