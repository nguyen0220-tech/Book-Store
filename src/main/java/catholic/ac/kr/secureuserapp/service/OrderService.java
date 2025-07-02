package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.OrderStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.OrderMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.OrderDTO;
import catholic.ac.kr.secureuserapp.model.dto.OrderRequest;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.plaf.PanelUI;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public ApiResponse<OrderDTO> checkout(OrderRequest request) {
        Cart cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow( ()-> new ResourceNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()){
            return ApiResponse.error("Cart is empty");
        }

        //total price
        BigDecimal total = cartItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        //create order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(total);
        order.setShippingAddress(request.getShippingAddress());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(book.getTitle()+" không đủ số lượng tồn");
            }

            book.setStock(book.getStock() - cartItem.getQuantity());
            bookRepository.save(book);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setBook(book);
            item.setQuantity(cartItem.getQuantity());
            item.setPrice(book.getPrice());


            orderItems.add(item);
        }

        order.setOrderItems(orderItems);
        orderRepository.save(order);

        cartItemRepository.deleteAll(cartItems); //clear caer

        return ApiResponse.success("Order successfully",orderMapper.toOrderDTO(order));
    }


    public ApiResponse<List<OrderDTO>> getOrderByUserId(Long userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);

        return ApiResponse.success("success", orderDTOS);
    }

    //only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDTO> updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        orderRepository.save(order);

        return ApiResponse.success("Updated order status successfully", orderMapper.toOrderDTO(order));
    }
}
