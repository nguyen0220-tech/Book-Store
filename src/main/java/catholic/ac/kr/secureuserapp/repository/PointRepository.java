package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.PointDTO;
import catholic.ac.kr.secureuserapp.model.dto.PointHistory;
import catholic.ac.kr.secureuserapp.model.entity.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PointRepository extends JpaRepository<Point, Long> {

    Optional<Point> findByUserId(Long userId);

    @Query("""
            SELECT p FROM Point p WHERE p.user.username = :username
            """)
    Optional<Point> findPointByUsername(@Param("username") String username);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.PointDTO(
            p.user.username, p.user.fullName, p.point, p.updatedAt)
            FROM Point p
            WHERE p.point > 0
            ORDER BY p.point DESC
            """)
    Page<PointDTO> findAllPoint(Pageable pageable);


    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.PointHistory(
            o.createdAt, o.pointHoard, o.pointUsage)
            FROM Order o
            WHERE o.user.id = :userId
            """)
    Page<PointHistory> findPointHistoryByUserId(@Param("userId") Long userId, Pageable pageable);
}
