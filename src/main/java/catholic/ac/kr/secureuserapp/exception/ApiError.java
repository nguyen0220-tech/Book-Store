package catholic.ac.kr.secureuserapp.exception;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiError {
    LocalDateTime timestamp;
    int status;
    String message;
    Map<String, String> detailsError;

    public ApiError(int value, String s) {
    }
}
