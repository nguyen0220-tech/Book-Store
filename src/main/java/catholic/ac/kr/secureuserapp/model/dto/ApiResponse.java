package catholic.ac.kr.secureuserapp.model.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    boolean success;
    String message;
    T data;
    LocalDateTime timestamp=LocalDateTime.now();

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }


    public static <T> ApiResponse<T> success(String message,T data) {
        return new ApiResponse<>(true,message,data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true,message,null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false,message,null);
    }
}
