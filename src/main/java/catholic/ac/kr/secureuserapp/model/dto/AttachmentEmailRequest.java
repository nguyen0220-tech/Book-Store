package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AttachmentEmailRequest {
    private String to;
    private String subject;
    private String body;
    private MultipartFile filePath;
}
