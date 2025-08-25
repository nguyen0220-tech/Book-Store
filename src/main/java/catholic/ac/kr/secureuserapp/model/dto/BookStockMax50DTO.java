package catholic.ac.kr.secureuserapp.model.dto;

public record BookStockMax50DTO(
        Long bookId,
        String title,
        String author,
        int stock,
        String imgUrl
){}