package catholic.ac.kr.secureuserapp.model.dto;

import catholic.ac.kr.secureuserapp.model.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class RefreshTokenDTO {
    private Long id;

    private String token;

    private LocalDateTime expiryDate;


    private boolean revoked; // token đã bị thu hồi chưa

    private LocalDateTime createdAt;

    private String deviceId;

    private String userAgent;

    private String ipAddress;
}
