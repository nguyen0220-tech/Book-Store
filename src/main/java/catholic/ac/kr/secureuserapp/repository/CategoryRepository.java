package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findById(Long id);

    @Query("""
            SELECT c FROM Category c
            JOIN Book b
            ON c.id = b.category.id
            WHERE b.id = :bookId
            """)
    Category findByBookId(@Param("bookId") Long bookId);

    Optional<Category> findByName(String name);

}
