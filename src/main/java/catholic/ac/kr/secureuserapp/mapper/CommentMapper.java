package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.CommentDTO;
import catholic.ac.kr.secureuserapp.model.entity.Comment;

public class CommentMapper {
    public static CommentDTO toCommentDTO(Comment comment) {
        CommentDTO commentDTO = new CommentDTO();

        commentDTO.setId(comment.getId());
        commentDTO.setPostId(comment.getPost().getId());
        commentDTO.setUserId(comment.getUser().getId());
        commentDTO.setUsername(comment.getUser().getUsername());
        commentDTO.setCommentContent(comment.getCommentContent());
        commentDTO.setCreatedAt(comment.getCreatedAt());

        return commentDTO;
    }
}
