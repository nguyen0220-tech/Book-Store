package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false,name = "username")
    private String username;

    @Column(nullable = false,name = "password")
    private String password;

 //   @Builder.Default // nếu không truyền thì mặc định là USER
    @Column(name = "role")
    private String role;
}
