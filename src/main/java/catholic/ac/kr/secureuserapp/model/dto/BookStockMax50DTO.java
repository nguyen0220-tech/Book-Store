package catholic.ac.kr.secureuserapp.model.dto;

public record BookStockMax50DTO(
        Long bookId,
        String title,
        int stock,
        String imgUrl
){}