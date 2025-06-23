package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.mapper.RefreshTokenMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RefreshTokenDTO;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenMapper refreshTokenMapper;

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public ApiResponse<List<RefreshTokenDTO>> getAllByUser(User user) {
        List<RefreshToken> refreshTokenList = refreshTokenRepository.findAllByUser(user);
        List<RefreshTokenDTO> refreshTokenDTOList = refreshTokenMapper.toDTO(refreshTokenList);
        return ApiResponse.success("Success", refreshTokenDTOList);
    }

    public ApiResponse<RefreshToken> createRefreshToken(User user, String deviceId, String userAgent, String ipAddress) {
//        refreshTokenRepository.deleteByUser(user); // nếu dung đa thiết bi thì không đuoc xóa
//        refreshTokenRepository.flush(); //phương thức của EntityManager (và JpaRepository) để ép JPA đồng bộ dữ liệu từ bộ nhớ tạm (persistence context) xuống database ngay lập tức

        Optional<RefreshToken> existingTokenOpt = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);

        existingTokenOpt.ifPresent(old -> {
            old.setRevoked(true);
            refreshTokenRepository.save(old);
        });

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(3));
        token.setDeviceId(deviceId);
        token.setUserAgent(userAgent);
        token.setIpAddress(ipAddress);
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(token);

        return ApiResponse.success("Refresh token created", token);
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    // Revoke token (thu hồi) khi đã sử dụng xong
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public boolean revokeByDeviceId(User user, String deviceId) {
        Optional<RefreshToken> token = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);
        if (token.isPresent()) {
            token.get().setRevoked(true);
            refreshTokenRepository.save(token.get());
            return true;
        }
        return false;
    }
}
