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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public ApiResponse<OrderDTO> checkout(Long userId, OrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            return ApiResponse.error("Cart is empty");
        }

        //total price
        BigDecimal total = cartItems.stream()
                .map(item -> item.getBook().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = total;
        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCouponCode(request.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
            finalTotal = applyCoupon(total, coupon, userId);
        }

        //create order
        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(finalTotal);
        order.setTotalDiscount(total.subtract(finalTotal));
        order.setShippingAddress(request.getShippingAddress());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());

        if (coupon != null) {
            order.setCoupon(coupon);
        }

        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Book book = cartItem.getBook();
            if (book.getStock() < cartItem.getQuantity()) {
                throw new RuntimeException(book.getTitle() + " không đủ số lượng tồn");
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

        return ApiResponse.success("Order successfully", orderMapper.toOrderDTO(order));
    }

    private BigDecimal applyCoupon(BigDecimal total, Coupon coupon, Long userId) {
        if (!coupon.isActive() || coupon.getExpired().isBefore(LocalDateTime.now()))
            throw new RuntimeException("Coupon expired");

        if (coupon.getUsageCount() >= coupon.getMaxUsage())
            throw new RuntimeException("Coupon exceeded max usage");

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!coupon.getUsers().contains(user))
            throw new RuntimeException("Coupon does not exist");

        if (total.compareTo(coupon.getMinimumAmount()) < 0)
            throw new RuntimeException("Total amount is less than minimum required for coupon");

        if (!coupon.isUsage() && couponRepository.countUsageCouponByUserId(coupon.getCouponCode(),userId) >= 1) {
            throw new RuntimeException("Coupon already used");
        }

        BigDecimal discount = coupon.isPercentDiscount()
                ? total.multiply(coupon.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountAmount();

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        return total.subtract(discount);
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
