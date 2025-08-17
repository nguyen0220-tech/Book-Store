package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.Status.PostShare;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.CommentMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CommentDTO;
import catholic.ac.kr.secureuserapp.model.entity.Comment;
import catholic.ac.kr.secureuserapp.model.entity.Post;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.CommentRepository;
import catholic.ac.kr.secureuserapp.repository.FriendRepository;
import catholic.ac.kr.secureuserapp.repository.PostRepository;
import catholic.ac.kr.secureuserapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final FriendRepository friendRepository;
    private final NotificationService notificationService;

    public ApiResponse<Page<CommentDTO>> getAllComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);

        Page<CommentDTO> commentDTOS = comments.map(CommentMapper::toCommentDTO);

        return ApiResponse.success("All comments", commentDTOS);
    }

    public ApiResponse<CommentDTO> createComment(Long userId, Long postId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean isFriend = friendRepository.existsByUserIdAndFriendIdAndStatus(userId, post.getUser().getId(), FriendStatus.FRIEND);
        boolean isPoster = userId.equals(post.getUser().getId());

        if (post.getPostShare() == PostShare.PRIVATE) {
            return ApiResponse.error("Đây la bài viết riêng tư không thể bình luận");
        } else if (post.getPostShare() == PostShare.FRIEND) {
            if (isFriend || isPoster) {
                Comment comment = initComment(userId, postId, content);
                commentRepository.save(comment);

                if (!userId.equals(post.getUser().getId())) {
                    notificationService.createCommentPostNotification(userId, post.getUser().getId(), comment);
                }

                return ApiResponse.success("Comment created", CommentMapper.toCommentDTO(comment));
            }
            return ApiResponse.error("Các bạn không phải bạn bè nên không thể bình luận");

        } else if (post.getPostShare() == PostShare.PUBLIC) {
            Comment comment = initComment(userId, postId, content);

            commentRepository.save(comment);

            if (!userId.equals(post.getUser().getId())) {
                notificationService.createCommentPostNotification(userId, post.getUser().getId(), comment);
            }
            return ApiResponse.success("Comment created", CommentMapper.toCommentDTO(comment));
        }
        return null;
    }

    private Comment initComment(Long userId, Long postId, String content) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        return Comment.builder()
                .post(post)
                .user(user)
                .commentContent(content)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    public ApiResponse<String> deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndPostId(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        boolean isCommenter = userId.equals(comment.getUser().getId());

        boolean isPoster = userId.equals(comment.getPost().getUser().getId());

        if (isCommenter || isPoster) {
            commentRepository.delete(comment);
            return ApiResponse.success("deleted comment");
        }

        return ApiResponse.error(" Không có quyền xóa bình luận này");
    }
}
