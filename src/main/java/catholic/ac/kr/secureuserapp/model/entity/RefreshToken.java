package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String token;

    private LocalDateTime expiryDate;

    @Column(nullable = false)
    private boolean revoked = false; // token đã bị thu hồi chưa

    private LocalDateTime createdAt = LocalDateTime.now();
}



