package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Rank;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends JpaRepository<Rank, Long> {

    @Query("""
            SELECT r FROM Rank r WHERE r.user.id = :userId
            """)
    Optional<Rank> findByUserId(@Param("userId") Long userId);

    boolean existsByUser(User user);

    Rank findByUser(User user);
}
