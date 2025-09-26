package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Table(name = "chat_room")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(unique = true)
    private String chatRoomName;

    @ManyToMany
    @JoinTable(
            name = "chat_room_members",
            joinColumns = @JoinColumn(name = "chat_room_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "owner_id")
    private User owner;

    private Timestamp createdAt;

    @Column(nullable = false,columnDefinition = "boolean default false")
    private boolean deleted;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    @PrePersist
    protected void onCreated(){createdAt = new Timestamp(System.currentTimeMillis());}

    @PreUpdate
    protected void onUpdated(){createdAt = new Timestamp(System.currentTimeMillis());}

}
