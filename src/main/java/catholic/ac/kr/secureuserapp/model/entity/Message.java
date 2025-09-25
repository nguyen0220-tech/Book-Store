package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.ChatType;
import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private boolean fromAdmin;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatType type;

}
