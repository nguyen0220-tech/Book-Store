package catholic.ac.kr.secureuserapp.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter @Setter
public class PostRequest {
    private String content;
    private String postShare;
    private String imageUrl;
}
