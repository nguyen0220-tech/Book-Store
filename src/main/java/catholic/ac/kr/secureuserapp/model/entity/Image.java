package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.ImageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    private Long referenceId;

    @Enumerated(EnumType.STRING)
    private ImageType type;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean isSelected; //sử dụng xác định user dùng ảnh làm avatar

    private LocalDateTime uploadAt;

    @PrePersist
    protected void uploadAt(){
        uploadAt = LocalDateTime.now();
    }
}
