package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "point", uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Point {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.EAGER,optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal point;

    private Timestamp updatedAt;

}
