package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.UserDTO;
import catholic.ac.kr.secureuserapp.model.entity.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Page<User> findAll(Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword%")
    Page<User> searchByName(@Param("keyword") String keyword, Pageable pageable);

    Page<User> findByRole(String role, Pageable pageable);

    Optional<User> findByUsername(@NotBlank(message = "Tên không được để trống") String username);


}
