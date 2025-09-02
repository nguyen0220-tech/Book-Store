package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.PostShare;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private String content;

    @Enumerated(EnumType.STRING)
    private PostShare postShare;

    @Column(nullable = false)
    private Timestamp postDate;

    private String imageUrl;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean deleted;

    private LocalDateTime expiryRestore;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

}
