package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.TopBookDTO;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER( b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> findByAuthor(@Param("author") String author, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE LOWER(b.category.name) LIKE LOWER(CONCAT('%', :category, '%'))")
    Page<Book> findByCategory(@Param("category") String category, Pageable pageable);

    //random books
    @Query(value = "SELECT * FROM Book ORDER BY random() LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Book> findRandomBooks(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM Book", nativeQuery = true)
    int countAllBooks();

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.TopBookDTO (
            i.book.id, i.book.title, i.book.price,i.book.salePrice ,i.book.imgUrl, SUM(i.quantity))
            FROM OrderItem i
            GROUP BY i.book.id, i.book.title, i.book.price,i.book.salePrice, i.book.imgUrl
            ORDER BY SUM(i.quantity) DESC
            """)
    List<TopBookDTO> findTop5SellingBooks(Pageable pageable);

    @Query("""
                SELECT new catholic.ac.kr.secureuserapp.model.dto.TopBookDTO(
                b.id, b.title,b.price,b.salePrice,b.imgUrl
                )
                FROM  Book b
                GROUP BY b.id,b.title,b.price,b.salePrice,b.imgUrl
                ORDER BY b.createdAt DESC
            """)
    List<TopBookDTO> findTop5NewBooks(Pageable pageable);
}
