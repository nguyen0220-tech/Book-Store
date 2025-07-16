package catholic.ac.kr.secureuserapp.model.dto;


import java.math.BigDecimal;

/*
record là một cú pháp đặc biệt trong Java (từ Java 14+ preview,
 và chính thức từ Java 16), được dùng để khai báo class bất biến (immutable) một cách ngắn gọn
 — thường dùng cho các DTO, response, hoặc data holder.
 - equals(), hashCode(), toString() cũng được sinh tự động
 Lưu ý:
Các field trong record là private final.
Không có setter.
Dùng tốt với @Query("SELECT new TopBookDTO(...)") khi truy vấn dữ liệu từ DB.

 */
public record TopBookDTO(
        Long bookId,
        String title,
        BigDecimal price,
        String imgUrl,
        Long totalSold
) {
    public TopBookDTO(Long bookId, String title, BigDecimal price, String imgUrl) {
        this(bookId, title, price, imgUrl, 0L);
    }
}
