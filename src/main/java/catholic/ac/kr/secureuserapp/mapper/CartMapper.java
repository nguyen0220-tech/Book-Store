package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.CartDTO;
import catholic.ac.kr.secureuserapp.model.dto.CartItemDTO;
import catholic.ac.kr.secureuserapp.model.entity.Cart;
import catholic.ac.kr.secureuserapp.model.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CartMapper {
    @Mapping(source = "book.id",target = "bookId")
    @Mapping(source = "book.title",target = "title")
    @Mapping(source = "book.price",target = "price")
    @Mapping(source = "book.imgUrl",target = "imgUrl")
    CartItemDTO toCartItemDTO(CartItem cartItem);

    // DTO → Entity: khai báo không ánh xạ book vì cần fetch từ DB riêng trong service,nếu không khai báo sẽ báo lỗi
    @Mapping(target = "book",ignore = true)
    @Mapping(target = "cart",ignore = true)
    CartItem toCartItem(CartItemDTO cartItemDTO);

    @Mapping(source = "user.id",target = "userId")
    CartDTO toCartDTO(Cart cart);

    @Mapping(target = "user",ignore = true)
    Cart toCart(CartDTO cartDTO);

    List<CartItemDTO> toCartItemDTOList(List<CartItem> cartItemList);
}
