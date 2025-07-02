package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.OrderDTO;
import catholic.ac.kr.secureuserapp.model.dto.OrderItemDTO;
import catholic.ac.kr.secureuserapp.model.entity.Order;
import catholic.ac.kr.secureuserapp.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "user.id",target = "userId")
    @Mapping(source = "createdAt", target = "orderDate")
    @Mapping(source = "id",target = "orderId")
    @Mapping(source = "status", target = "orderStatus")
    @Mapping(source = "orderItems", target = "items")
    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "user", ignore = true)
    Order toOrder(OrderDTO orderDTO);

    @Mapping(source = "book.id",target = "bookId")
    @Mapping(source = "book.title",target = "title")
    @Mapping(source = "book.price",target = "price")
    @Mapping(source = "book.imgUrl",target = "imgUrl")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);

    @Mapping(target = "book",ignore = true)
    @Mapping(target = "order",ignore = true)
    OrderItem toOrderItem(OrderItemDTO orderItemDTO);

    List<OrderDTO> toOrderDTO(List<Order> orders);
}
