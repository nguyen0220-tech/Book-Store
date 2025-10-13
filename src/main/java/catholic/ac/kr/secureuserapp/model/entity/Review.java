package catholic.ac.kr.secureuserapp.model.entity;

import catholic.ac.kr.secureuserapp.Status.Rating;
import catholic.ac.kr.secureuserapp.convert.RatingConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter @Setter
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id",nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY) //một đơn hàng có thể chứa nhiều sách, và mỗi sách cần 1 review riêng biệt
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false,length = 2000)
    private String content;

    private String imageReviewUrl;

    @Convert(converter = RatingConverter.class)
    private Rating rating;

    @Column(nullable = false)
    private Timestamp createdAt;
}
