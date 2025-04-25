package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

@Data
public class AttachmentEmailRequest {
    private String to;
    private String subject;
    private String body;
    private String filePath;
}
