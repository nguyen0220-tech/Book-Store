package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.mapper.RefreshTokenMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.RefreshTokenDTO;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
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
        // Tìm tất cả token của cùng user + deviceId
        List<RefreshToken> existingTokens = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);

        if (!existingTokens.isEmpty()) {
            for (RefreshToken old : existingTokens) {
                if (!old.isRevoked()) {
                    old.setRevoked(true);
                }
            }
            refreshTokenRepository.saveAll(existingTokens); // Lưu lại tất cả sau khi revoke
        }

        // Tạo token mới
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(generateUniqueToken());
        token.setExpiryDate(LocalDateTime.now().plusDays(3));
        token.setDeviceId(deviceId);
        token.setUserAgent(userAgent);
        token.setIpAddress(ipAddress);
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());

        refreshTokenRepository.save(token);
        return ApiResponse.success("Refresh token created", token);
    }

    private String generateUniqueToken() {
        String token;
        do {
            token = UUID.randomUUID().toString();
        } while (refreshTokenRepository.findByToken(token).isPresent());
        return token;
    }

    public boolean isValid(RefreshToken token) {
        return token != null && !token.isRevoked() && token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
    }

    @Transactional
    public boolean revokeByDeviceId(User user, String deviceId) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndDeviceId(user, deviceId);
        if (tokens.isEmpty()) return false;

        for (RefreshToken token : tokens) {
            token.setRevoked(true);
        }
        refreshTokenRepository.saveAll(tokens);
        return true;
    }
}
