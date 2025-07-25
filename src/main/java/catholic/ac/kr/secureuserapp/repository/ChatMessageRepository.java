package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findBySenderUsernameAndRecipientUsername(
            @Param("senderUsername") String senderUsername,
            @Param("recipientUsername") String recipientUsername,
            Pageable pageable);

    @Query("""
            SELECT m FROM ChatMessage m
            WHERE (m.sender.username = :senderUsername AND m.recipient.username = :recipientUsername)
            OR (m.sender.username = :recipientUsername AND m.recipient.username = :senderUsername)
            ORDER BY m.timestamp ASC
            """)
    Page<ChatMessage> findTwoWayMessage(
            @Param("senderUsername") String senderUsername,
            @Param("recipientUsername") String recipientUsername,
            Pageable pageable);
}
