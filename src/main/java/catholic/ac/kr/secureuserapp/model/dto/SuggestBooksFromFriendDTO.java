package catholic.ac.kr.secureuserapp.model.dto;

import java.math.BigDecimal;


public record SuggestBooksFromFriendDTO (
     Long id,
     String title,
     String author,
     BigDecimal price,
     BigDecimal salePrice,
     String imgUrl,
     String friendName
){
}
