package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;
import lombok.Value;

@Data
public class EmailDTO {
    private String to;
    private String subject;
    private String body;
}
