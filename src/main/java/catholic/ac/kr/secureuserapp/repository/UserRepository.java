package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    @Query("SELECT u FROM User u WHERE LOWER( u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByName(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT u FROM User u JOIN  u.roles r WHERE LOWER( r.name) LIKE LOWER(CONCAT('%', :role, '%'))")
    Page<User> findByRole(@Param("role") String role, Pageable pageable);

    Optional<User> findByUsername(@NotBlank(message = "Tên không được để trống") String username);

    Optional<User> findByPhone(String phone);

    boolean existsByPhone(String phone);

    List<User> findByMonthOfBirthAndDayOfBirth(String monthOfBirth, String dayOfBirth);
}
