package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.RefreshToken;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    List<RefreshToken> findAllByUser(User user);

    Optional<RefreshToken> findByToken(String token);

    Optional<RefreshToken> findByUserAndDeviceId(User user,String deviceId);

    @Transactional
    void deleteByUser(User user);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    int deleteByToken(String token);

}
