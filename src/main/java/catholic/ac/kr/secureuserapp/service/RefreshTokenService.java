package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public ApiResponse<RefreshToken> createRefreshToken(User user) {
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(token);

        return ApiResponse.success("Refresh token created");
    }

    public boolean isValid(RefreshToken token) {
        return token.getExpiryDate().isAfter(LocalDateTime.now());
    }

    public Optional<RefreshToken> findRefreshToken(String token) {
        return  refreshTokenRepository.findByToken(token);
    }


    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
