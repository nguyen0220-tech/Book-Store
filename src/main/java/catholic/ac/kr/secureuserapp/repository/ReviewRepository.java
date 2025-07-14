package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBookId(Long bookId);

    @Query("""
            SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END
            FROM Review r
            WHERE r.user.id = :userId AND r.book.id = :bookId AND r.order.id = :orderId
            """) //CASE WHEN ... THEN true ELSE false END: là cách để trả về giá trị boolean thay vì số lượng.
    boolean existsByUserIdAndBookIdAndOrderId(@Param("userId") Long userId,
                                              @Param("bookId") Long bookId,
                                              @Param("orderId") Long orderId);

}
