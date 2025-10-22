package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.MessageStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

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

    private String imageUrl;

    @Column(nullable = false)
    private boolean fromAdmin;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus status;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageReply> messageReplies = new ArrayList<>();

}
