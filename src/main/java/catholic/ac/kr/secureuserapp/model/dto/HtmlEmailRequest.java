package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Data;

@Data
public class HtmlEmailRequest {
    private String to;
    private String subject;
    private String bodyHtml;
}
