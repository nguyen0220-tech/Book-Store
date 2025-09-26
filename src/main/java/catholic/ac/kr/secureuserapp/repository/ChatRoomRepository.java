package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    @Query("""
            SELECT r FROM ChatRoom r
            JOIN Message m ON r.id = m.chatRoom.id
            WHERE CONCAT(m.sender.username,'_',m.recipient.username) = :roomName
            OR CONCAT(m.recipient.username,'_',m.sender.username) = :roomName
            """)
    Optional<ChatRoom> findByRoomName(@Param("roomName") String roomName);

    @Query(value = "SELECT cr.* FROM chat_room cr " +
            " JOIN chat_room_members crm ON cr.id = crm.chat_room_id" +
            " WHERE crm.user_id = :userId AND cr.type = :type AND cr.deleted = false" +
            " ORDER BY cr.created_at DESC ",nativeQuery = true)
    Page<ChatRoom> findByUserId(@Param("userId") Long userId,@Param("type") String type, Pageable pageable);
}
