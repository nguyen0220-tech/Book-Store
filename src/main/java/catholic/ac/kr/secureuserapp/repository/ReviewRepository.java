package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.ReviewDetailDTO;
import catholic.ac.kr.secureuserapp.model.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(Long bookId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Review r
            WHERE r.user.id = :userId AND r.book.id = :bookId AND r.order.id = :orderId
            """)
        //CASE WHEN ... THEN true ELSE false END: là cách để trả về giá trị boolean thay vì số lượng.
    boolean existsByUserIdAndBookIdAndOrderId(@Param("userId") Long userId,
                                              @Param("bookId") Long bookId,
                                              @Param("orderId") Long orderId);

    @Query("""
            SELECT AVG(r.rating) FROM Review r WHERE r.book.id = :bookId
            """)
    Double getAvgRatingByBookId(@Param("bookId") Long bookId);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.ReviewDetailDTO(
            r.user.id,r.user.fullName,r.rating,r.content,r.imageReviewUrl,r.createdAt
                        )
            FROM Review r
            WHERE r.book.id = :bookId
            """)
    Page<ReviewDetailDTO> getReviewDetailDTOByBookI(@Param("bookId") Long bookId, Pageable pageable);

    @Query("""
            SELECT DISTINCT r.user.id FROM Review r WHERE r.book.id = :bookId
            """)
    Set<Long> findUserIdReviewedByBookId(@Param("bookId") Long bookId);
}
