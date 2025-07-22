package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Book;
import catholic.ac.kr.secureuserapp.model.entity.BookMark;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    List<BookMark> findByUserId(Long userId);

    @Query("""
            SELECT bm FROM BookMark bm WHERE bm.book.id = :bookId
            """)
    List<BookMark> findByBookId(@Param("bookId") Long bookId);

    @Query("""
            SELECT CASE WHEN COUNT (bm)>0 THEN true ELSE false END
            FROM BookMark bm
            WHERE bm.user.id = :userId AND bm.book.id = :bookId
            """)
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    @Query("""
            SELECT CASE WHEN COUNT (bm)>0 THEN true ELSE false END
            FROM BookMark bm
            where bm.user = :user AND bm.book = :book
            """)
    boolean existsByUserAndBook(@Param("user") User user,@Param("book") Book book);


    @Modifying
    @Query("DELETE FROM BookMark bm WHERE bm.user.id = :userId AND bm.book.id = :bookId ")
    void deleteByUserIdAndBookId(Long userId, Long bookId);
}
