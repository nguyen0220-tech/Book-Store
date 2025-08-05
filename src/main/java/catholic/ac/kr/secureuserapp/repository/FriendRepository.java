package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.model.entity.Friend;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {
    boolean existsByUserIdAndFriendIdAndStatus(Long userId, Long friendId, FriendStatus status);

    @Query("""
            SELECT f FROM Friend f
            WHERE (f.user.id=:userId AND f.friend.id = :friendId AND f.status = :status)
            OR (f.friend.id=:userId AND f.user.id = :friendId AND f.status = :status)
            """)
    Optional<Friend> findByUserIdAndFriendIdAndStatus(@Param("userId") Long userId, @Param("friendId") Long friendId,@Param("status") FriendStatus status);

    @Query("""
            SELECT f FROM Friend f
            WHERE (f.user.id=:userId AND f.friend.id = :friendId AND f.status = :status)
            OR (f.friend.id=:userId AND f.user.id = :friendId AND f.status = :status)
            """)
    List<Friend> findByUserIdAndFriendIdAndStatusFRIEND(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") FriendStatus status);

    @Query("""
            SELECT f FROM Friend f
            WHERE (f.user.id=:userId AND f.friend.id = :friendId AND f.status = :status)
            OR (f.friend.id=:userId AND f.user.id = :friendId AND f.status = :status)
            """)
    List<Friend> findByUserIdAndFriendIdAndStatusBLOCKED(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") FriendStatus status);

    Page<Friend> findByUserIdAndStatus(Long userId, FriendStatus status, Pageable pageable);

    Page<Friend> findByFriendIdAndStatus(Long userId, FriendStatus status, Pageable pageable);

    int countByUserIdAndStatus(Long userId, FriendStatus status);
}
