package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    List<RefreshToken> findByUserAndDeviceId(User user,String deviceId);

    @Transactional
    int deleteByToken(String token);

    @Modifying
    @Query("""
            DELETE FROM RefreshToken t WHERE t.expiryDate <= :now
            """)
    void deleteRefreshTokenExpired(@Param("now") LocalDateTime now);

}
