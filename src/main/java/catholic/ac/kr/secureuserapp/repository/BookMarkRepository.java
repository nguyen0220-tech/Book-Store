package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.BookMark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    List<BookMark> findByUserId(Long userId);

    @Query("""
            SELECT CASE WHEN COUNT (bm)>0 THEN true ELSE false END 
            FROM BookMark bm
            WHERE bm.user.id = :userId AND bm.book.id = :bookId
            """)
    boolean existsByUserIdAndBookId(Long userId, Long bookId);


    @Modifying
    @Query("DELETE FROM BookMark bm WHERE bm.user.id = :userId AND bm.book.id = :bookId ")
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
