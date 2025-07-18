package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.SearchType;
import catholic.ac.kr.secureuserapp.model.entity.SearchHistory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
    @Query("""
            SELECT s
            FROM SearchHistory s
            WHERE s.user.id = :userId
            ORDER BY s.searchAt DESC
            """)
    List<SearchHistory> find5ResultSearchHistoryByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
            SELECT s FROM SearchHistory s
            WHERE s.user.id = :userId AND s.search = :keyword AND s.type = :type
            """)
    Optional<SearchHistory> findByUserIdAndSearch(@Param("userId") Long userId, @Param("keyword") String keyword, @Param("type")SearchType type);

    @Modifying
    @Query("""
            DELETE FROM SearchHistory s WHERE s.user.id = :userId AND s.search = :search
            """)
    void deleteByUserIdAndSearch(@Param("userId") Long userId,@Param("search") String search);

    @Modifying
    @Query("""
            DELETE FROM SearchHistory s WHERE s.user.id = :userId
            """)
    void deleteAllByUserId(@Param("userId") Long userId);
}
