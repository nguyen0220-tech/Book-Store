package catholic.ac.kr.secureuserapp.model.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PostRequest {
    private String content;
    private String postShare;
    private String imageUrl;
}
