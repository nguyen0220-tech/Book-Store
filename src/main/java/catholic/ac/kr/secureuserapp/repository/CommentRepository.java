package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.entity.Comment;
import catholic.ac.kr.secureuserapp.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    Page<Comment> findByPostId(Long postId, Pageable pageable);

    @Query("""
            SELECT DISTINCT c.user.id FROM Comment c WHERE c.post.id = :postId
            """)
    Set<Long> findAllUserCommentedByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT c FROM Comment c WHERE c.id=:id
            """)
    Optional<Comment> findByIdAndPostId(@Param("id") Long id);

}
