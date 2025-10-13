package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class PostRequest {
    private String content;
    private String postShare;
//    private String imageUrl;
    private MultipartFile file;
}
