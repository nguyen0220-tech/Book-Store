package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import catholic.ac.kr.secureuserapp.model.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Page<Message> findBySenderUsernameAndRecipientUsername(
            @Param("senderUsername") String senderUsername,
            @Param("recipientUsername") String recipientUsername,
            Pageable pageable);

    @Query("""
            SELECT m FROM Message m
            WHERE (m.sender.username = :senderUsername AND m.recipient.username = :recipientUsername)
            OR (m.sender.username = :recipientUsername AND m.recipient.username = :senderUsername)
            ORDER BY m.timestamp ASC
            """)
    Page<Message> findTwoWayMessage(
            @Param("senderUsername") String senderUsername,
            @Param("recipientUsername") String recipientUsername,
            Pageable pageable);

    @Modifying
    @Query("""
            UPDATE Message m SET m.status = :status
            WHERE m.sender.username = :user1 AND m.recipient.username = :user2 AND m.status <> :status
            """)
    int maskAsReal(@Param("user1") String user1,@Param("user2") String user2,@Param("status") MessageStatus status);
}
