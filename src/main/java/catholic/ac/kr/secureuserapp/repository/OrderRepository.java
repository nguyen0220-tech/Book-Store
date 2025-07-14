package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);

    @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Order o JOIN o.orderItems i WHERE o.user.id = :userId AND i.book.id = :bookId")
    boolean existsByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
}
