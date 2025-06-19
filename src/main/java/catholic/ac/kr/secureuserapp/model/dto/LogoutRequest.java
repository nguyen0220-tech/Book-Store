package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LogoutRequest {
    private String refreshToken;
}
