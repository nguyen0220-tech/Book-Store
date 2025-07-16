package catholic.ac.kr.secureuserapp.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "book_mark", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "book_id"}) //Thiết lập ràng buộc duy nhất (UNIQUE constraint) trên cặp cột user_id và book_id
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class BookMark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @Column(nullable = false)
    private Timestamp createdAt;
}
