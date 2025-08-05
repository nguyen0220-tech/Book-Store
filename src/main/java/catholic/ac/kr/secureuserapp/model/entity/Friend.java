package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
//@Table(
//        uniqueConstraints = {
//                @UniqueConstraint(columnNames = {"user_id", "friend_id"})
//        })
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Friend {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "friend_id")
    private User friend;

    @Enumerated(EnumType.STRING)
    private FriendStatus status;

    @Column(nullable = false,name = "created_at")
    private Timestamp createdAt;


}
