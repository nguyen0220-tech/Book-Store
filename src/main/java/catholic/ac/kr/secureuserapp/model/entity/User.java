package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(nullable = false, name = "username")
    private String username;

    @Column(nullable = false, name = "password")
    private String password;

    // Mỗi user có thể có nhiều role
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "user_id"),// FK user
            inverseJoinColumns = @JoinColumn(name = "role_id")// FK role
    )
    private Set<Role> roles;

    @Column(nullable = false)
    private boolean enabled = false; // mặc định false, chỉ khi xác thực email mới true
}
