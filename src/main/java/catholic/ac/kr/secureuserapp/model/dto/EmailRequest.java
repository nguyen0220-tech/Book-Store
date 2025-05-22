package catholic.ac.kr.secureuserapp.model.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailRequest {
    @Email(message = "Email không hợp lệ")
    private String to;
    private String subject;
    private String body;
}
