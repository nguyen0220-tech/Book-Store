package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter @Setter
public class MessageRequest {
    private String sender;
    private String recipient;
    private String message;
    private MultipartFile file;
}
