package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.NotificationType;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.Notification;
import catholic.ac.kr.secureuserapp.model.entity.Order;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    @Query("""
            SELECT n FROM Notification  n WHERE n.user.id = :userId AND n.id = :notificationId
            """)
    Optional<Notification> findByUserIdAndId(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    Page<Notification> findByUserId(Long userId, Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Notification n SET n.read=true WHERE n.user.id =:userId AND n.id=:notificationId
            """)
    int markAsRead(@Param("userId") Long userId, @Param("notificationId") Long notificationId);

    Page<Notification> findByUserIdAndType(Long user_id, NotificationType type, Pageable pageable);


    @Query("""
            SELECT COUNT(n) FROM Notification n WHERE n.user.id = :userId AND n.read = false
            """)
    int countUnreadNotificationsByUserId(@Param("userId") Long userId);

    @Query("""
            SELECT CASE WHEN COUNT(n)>0 THEN true ELSE false END
            FROM Notification n
            WHERE n.order= :order AND n.message =:message
            """)
    boolean exitsByOrderAndMessage(Order order, String message);

    @Query("""
                SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
                FROM Notification n
                WHERE n.user = :user AND n.book = :book AND n.message = :message
            """)
    boolean existsByBookAndMessage(@Param("user") User user, @Param("book") Book book, @Param("message") String message);

}
