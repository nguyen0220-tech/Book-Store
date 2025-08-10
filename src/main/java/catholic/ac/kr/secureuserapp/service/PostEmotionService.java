package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.EmotionStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.PostEmotionMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.PostEmotionCountDTO;
import catholic.ac.kr.secureuserapp.model.dto.PostEmotionDTO;
import catholic.ac.kr.secureuserapp.model.dto.PostEmotionFilterUserDTO;
import catholic.ac.kr.secureuserapp.model.entity.Post;
import catholic.ac.kr.secureuserapp.model.entity.PostEmotion;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.PostEmotionRepository;
import catholic.ac.kr.secureuserapp.repository.PostRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PostEmotionService {
    private final PostEmotionRepository postEmotionRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    //    su dung GROUP BY
    public ApiResponse<List<PostEmotionCountDTO>> getPostEmotionAndCount(Long postId) {

        List<PostEmotionCountDTO> postEmotionCount = postEmotionRepository.findPostEmotionCountByPostId(postId);

        return ApiResponse.success("get all post emotions", postEmotionCount);
    }

    public ApiResponse<List<PostEmotionFilterUserDTO>> getPostEmotionAndFilter(Long postId) {

        List<PostEmotionFilterUserDTO> postEmotionFilterUserDTOS = postEmotionRepository.findPostEmotionAndFilterByPostId(postId);

        return ApiResponse.success("get all post emotions and filter", postEmotionFilterUserDTOS);
    }

    public ApiResponse<PostEmotionDTO> createPostEmotion(Long userId, Long postId, EmotionStatus status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("post not found"));

        boolean isEmotion = postEmotionRepository.existsByUserIdAndPostId(user.getId(), post.getId());

        if (isEmotion) {
            return ApiResponse.error("Bạn đã thể hiện cảm xúc bài viết rồi");
        }

        PostEmotion postEmotion = new PostEmotion();
        postEmotion.setUser(user);
        postEmotion.setPost(post);
        postEmotion.setEmotionStatus(status);
        postEmotion.setCreateAt(new Timestamp(System.currentTimeMillis()));

        postEmotionRepository.save(postEmotion);

        PostEmotionDTO postEmotionDTO = PostEmotionMapper.toPostEmotionDTO(postEmotion);

        return ApiResponse.success("created post emotion", postEmotionDTO);
    }

    public ApiResponse<PostEmotionDTO> updatePostEmotion(Long userId, Long postId, EmotionStatus status) {
        PostEmotion postEmotion = postEmotionRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> new ResourceNotFoundException("post emotion not found"));

        postEmotion.setEmotionStatus(status);

        postEmotionRepository.save(postEmotion);

        PostEmotionDTO postEmotionDTO = PostEmotionMapper.toPostEmotionDTO(postEmotion);

        return ApiResponse.success("updated post emotion", postEmotionDTO);
    }

    @Transactional
    public ApiResponse<String> deletePostEmotion(Long userId, Long postId) {
        PostEmotion postEmotion = postEmotionRepository.findByUserIdAndPostId(userId,postId)
                .orElseThrow(()->new ResourceNotFoundException("post emotion not found"));

        postEmotionRepository.delete(postEmotion);

        return ApiResponse.success("deleted post emotion");
    }

}
