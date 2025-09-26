package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "message_reply")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageReply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String messageReply;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "message_id")
    private Message message;

    private Timestamp replyTime;

    @PrePersist
    protected void onReply() {
        replyTime = new Timestamp(System.currentTimeMillis());
    }
}
