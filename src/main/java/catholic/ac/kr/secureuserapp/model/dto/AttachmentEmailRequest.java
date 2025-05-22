package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AttachmentEmailRequest {
    @Email(message = "Email không hợp lệ")
    private String to;
    private String subject;
    private String body;
    private MultipartFile filePath;
}
