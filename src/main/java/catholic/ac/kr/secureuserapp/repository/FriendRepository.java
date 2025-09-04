package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.model.dto.ToGiveFriendDTO;
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
    List<Friend> findByUserIdAndFriendIdAndStatusTwoWay(@Param("userId") Long userId, @Param("friendId") Long friendId,@Param("status") FriendStatus status);

    @Query("""
            SELECT f FROM Friend f
            WHERE f.user.id=:userId AND f.friend.id = :friendId AND f.status = :status
            """)
    Optional<Friend> findByUserIdAndFriendIdAndStatusOneWay(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("status") FriendStatus status);

    Page<Friend> findByUserIdAndStatus(Long userId, FriendStatus status, Pageable pageable);

    Page<Friend> findByFriendIdAndStatus(Long userId, FriendStatus status, Pageable pageable);

    int countByUserIdAndStatus(Long userId, FriendStatus status);

    @Query("""
            SELECT COUNT(f) FROM User u
            JOIN Friend f ON u.id = f.user.id
            WHERE f.friend.id = :userId AND f.status = 'PENDING'
            """) // khi a->b thì b là người nhận lời mời nên lấy userId của b
    int countRequestFriendByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.ToGiveFriendDTO(
                        f.friend.fullName,
                        f.friend.phone,
                        f.friend.address)
            FROM User u
            JOIN Friend f ON u.id = f.user.id
            WHERE u.id = :userId AND f.status = 'FRIEND'
            """)
    List<ToGiveFriendDTO> findToGiveFriends(@Param("userId") Long userId);
}
