package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.*;
import catholic.ac.kr.secureuserapp.model.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Book> findByTitle(@Param("title") String title, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND LOWER( b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
    Page<Book> findByAuthor(@Param("author") String author, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isDeleted = false AND LOWER(b.category.name) LIKE LOWER(CONCAT('%', :category, '%'))")
    Page<Book> findByCategory(@Param("category") String category, Pageable pageable);

//    @Query("""
//            SELECT new catholic.ac.kr.secureuserapp.model.dto.BookDetailDTO(
//            b.id,b.title,b.author,b.price,b.salePrice,b.saleExpiry,
//            b.description,b.imgUrl,b.category.name,avg(r.rating))
//            FROM Book b
//            JOIN Review r ON b.id = r.book.id
//            WHERE b.id = :bookId
//            """)
//    BookDetailDTO findBookDetailById(@Param("bookId") Long bookId);

    //random books
    @Query(value = "SELECT * FROM Book b WHERE b.is_deleted = false ORDER BY random() LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Book> findRandomBooks(@Param("limit") int limit, @Param("offset") int offset);

    @Query(value = "SELECT COUNT(*) FROM Book", nativeQuery = true)
    int countAllBooks();

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.TopBookDTO (
            i.book.id, i.book.title, i.book.price,i.book.salePrice ,i.book.imgUrl, SUM(i.quantity))
            FROM OrderItem i
            WHERE i.book.isDeleted = false
            GROUP BY i.book.id, i.book.title, i.book.price,i.book.salePrice, i.book.imgUrl
            ORDER BY SUM(i.quantity) DESC
            """)
    List<TopBookDTO> findTop5SellingBooks(Pageable pageable);

    @Query("""
                SELECT new catholic.ac.kr.secureuserapp.model.dto.TopBookDTO(
                b.id, b.title,b.price,b.salePrice,b.imgUrl
                )
                FROM  Book b
                WHERE b.isDeleted = false
                GROUP BY b.id,b.title,b.price,b.salePrice,b.imgUrl
                ORDER BY b.createdAt DESC
            """)
    List<TopBookDTO> findTop5NewBooks(Pageable pageable);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.BookStockMax50DTO(
            b.id, b.title,b.author, b.stock, b.imgUrl)
            FROM Book b
            WHERE b.stock <= 50 AND b.isDeleted = false
            GROUP BY b.id, b.title, b.stock, b.imgUrl,b.author
            ORDER BY b.stock DESC
            """)
    Page<BookStockMax50DTO> findBooksByStockMax50(Pageable pageable);

    @Query("""
            SELECT DISTINCT new catholic.ac.kr.secureuserapp.model.dto.SuggestBooksFromFriendDTO(
            b.id,b.title,b.author,b.price,b.salePrice,b.imgUrl,f.user.fullName )
            FROM Book b
            JOIN OrderItem oi ON b.id = oi.book.id
            JOIN Order o ON oi.order.id = o.id
            JOIN User u ON o.user.id = u.id
            JOIN Friend f ON u.id = f.user.id
            WHERE f.friend.id = :userId AND f.status = 'FRIEND' AND b.isDeleted = false
            AND b.id NOT IN (SELECT b.id FROM Book b
                        JOIN OrderItem oi ON b.id = oi.book.id
                        JOIN Order o ON oi.order.id = o.id
                        WHERE o.user.id = :userId)
            """)
    Page<SuggestBooksFromFriendDTO> findSuggestBooksFromFriends(@Param("userId") Long userId,Pageable pageable);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.BookPaidMany(
            oi.book.id,oi.book.title,oi.book.price,oi.book.salePrice,SUM(oi.quantity),oi.book.imgUrl)
            FROM OrderItem oi
            WHERE oi.order.user.id = :userId AND oi.book.isDeleted = false
            GROUP BY oi.book.id,oi.book.title,oi.book.price,oi.book.salePrice,oi.book.imgUrl
            ORDER BY SUM(oi.quantity) DESC
            LIMIT 5
            """)
    List<BookPaidMany> findBookPaidMany(@Param("userId") Long userId);

    @Query("""
            SELECT b FROM Book b WHERE b.category.name = :category AND b.isDeleted = false
            """)
    List<Book> findBooksSuggestByCategory(@Param("category")String category);

    @Query("""
       SELECT b FROM Book b
       WHERE b.salePrice IS NOT NULL AND b.isDeleted = false
       AND b.saleExpiry < :expiry
       """)
    List<Book> findBooksSalePriceExpiry(@Param("expiry") LocalDate expiry);

    @Query("""
            SELECT b FROM Book b
            WHERE b.salePrice IS NOT NULL AND b.isDeleted = false AND b.saleExpiry > :expiry
            """)
    Page<Book> findBookHavingASale(@Param("expiry") LocalDate expiry,Pageable pageable);

    @Query("""
            SELECT b FROM Book b
            WHERE b.isDeleted = :isDeleted
            """)
    Page<Book> findByDeleted(@Param("isDeleted") boolean isDeleted, Pageable pageable);
}
