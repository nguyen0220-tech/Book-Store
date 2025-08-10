package catholic.ac.kr.secureuserapp.repository;

import catholic.ac.kr.secureuserapp.model.dto.PostEmotionCountDTO;
import catholic.ac.kr.secureuserapp.model.dto.PostEmotionFilterUserDTO;
import catholic.ac.kr.secureuserapp.model.entity.PostEmotion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostEmotionRepository extends JpaRepository<PostEmotion, Long> {

    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<PostEmotion> findByUserIdAndPostId(Long userId, Long postId);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.PostEmotionCountDTO(pe.emotionStatus,COUNT(pe.emotionStatus))
            FROM PostEmotion pe
            WHERE pe.post.id = :postId
            GROUP BY pe.emotionStatus
            """)
    List<PostEmotionCountDTO> findPostEmotionCountByPostId(@Param("postId") Long postId);

    @Query("""
            SELECT new catholic.ac.kr.secureuserapp.model.dto.PostEmotionFilterUserDTO(pe.user.id,pe.user.username,pe.emotionStatus)
            FROM PostEmotion pe
            WHERE pe.post.id = :postId
            """)
    List<PostEmotionFilterUserDTO> findPostEmotionAndFilterByPostId(@Param("postId") Long postId);
}
