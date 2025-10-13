package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class CommentRequest {
    private Long postId;
    private String comment;
    private MultipartFile file;
}
