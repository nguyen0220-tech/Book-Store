package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.Status.ImageType;
import catholic.ac.kr.secureuserapp.Status.PostShare;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.CommentMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.CommentDTO;
import catholic.ac.kr.secureuserapp.model.dto.request.CommentRequest;
import catholic.ac.kr.secureuserapp.model.entity.Comment;
import catholic.ac.kr.secureuserapp.model.entity.Image;
import catholic.ac.kr.secureuserapp.model.entity.Post;
import catholic.ac.kr.secureuserapp.model.entity.User;
import catholic.ac.kr.secureuserapp.repository.*;
import catholic.ac.kr.secureuserapp.uploadhandler.UploadFileHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    private final UploadFileHandler uploadFileHandler;
    private final ImageRepository imageRepository;

    @Cacheable(value = "commentCache", key = "#postId")
    public ApiResponse<Page<CommentDTO>> getAllComments(Long postId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> comments = commentRepository.findByPostId(postId, pageable);

        Page<CommentDTO> commentDTOS = comments.map(CommentMapper::toCommentDTO);

        return ApiResponse.success("All comments", commentDTOS);
    }

    @CacheEvict(value = "commentCache", allEntries = true)
    public ApiResponse<CommentDTO> createComment(Long userId, CommentRequest request) {
        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        boolean isFriend = friendRepository.existsByUserIdAndFriendIdAndStatus(userId, post.getUser().getId(), FriendStatus.FRIEND);
        boolean isPoster = userId.equals(post.getUser().getId());

        if (post.getPostShare() == PostShare.PRIVATE) {
            return ApiResponse.error("Đây la bài viết riêng tư không thể bình luận");
        } else if (post.getPostShare() == PostShare.FRIEND) {
            if (isFriend || isPoster) {
                Comment comment = initComment(userId,request);

                if (!userId.equals(post.getUser().getId())) {
                    notificationService.createCommentPostNotification(userId, post.getUser().getId(), comment);
                }

                return ApiResponse.success("Comment created", CommentMapper.toCommentDTO(comment));
            }
            return ApiResponse.error("Các bạn không phải bạn bè nên không thể bình luận");

        } else if (post.getPostShare() == PostShare.PUBLIC) {
            Comment comment = initComment(userId, request);

            if (!userId.equals(post.getUser().getId())) {
                notificationService.createCommentPostNotification(userId, post.getUser().getId(), comment);
            }
            return ApiResponse.success("Comment created", CommentMapper.toCommentDTO(comment));
        }
        return null;
    }

    private Comment initComment(Long userId, CommentRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        Image image = new Image();

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setCommentContent(request.getComment());

        String imageUrl = request.getFile() != null ?
                uploadFileHandler.uploadFile(userId, request.getFile())
                : null;
        comment.setImageUrl(imageUrl);

        comment.setCreatedAt(new Timestamp(System.currentTimeMillis()));

        commentRepository.save(comment);

        if (imageUrl != null) {
            image.setUser(user);
            image.setImageUrl(imageUrl);
            image.setType(ImageType.COMMENT);
            image.setSelected(false);
            image.setReferenceId(comment.getId());

            imageRepository.save(image);
        }

        return comment;
    }

    @CacheEvict(value = "commentCache", allEntries = true)
    public ApiResponse<String> deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndPostId(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCommenter = userId.equals(comment.getUser().getId());

        boolean isPoster = userId.equals(comment.getPost().getUser().getId());

        if (isCommenter || isPoster || admin.getUsername().equals("admin")) {
            commentRepository.delete(comment);
            return ApiResponse.success("deleted comment");
        }

        return ApiResponse.error(" Không có quyền xóa bình luận này");
    }
}
