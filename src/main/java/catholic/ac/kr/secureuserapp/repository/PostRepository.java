package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {
    Page<Post> findByUserId(Long userId, Pageable pageable);

    @Query("""
            SELECT p FROM Post p WHERE p.user.id = :userId AND p.id = :postId
            """)
    Optional<Post> findByUserIdAndPostId(@Param("userId") Long userId,@Param("postId") Long postId);
}
