package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.model.entity.Image;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    @Query("""
        SELECT i FROM Image i
        WHERE i.user = :user AND i.isSelected = :selected
        """)
    Optional<Image> findByUserAndSelected(@Param("user") User user, @Param("selected") boolean selected);

    Page<Image> findByUserAndType(User user, ImageType type,
                                  Pageable pageable);

    Optional<Image> findByUserAndId(User user, Long id);
}
