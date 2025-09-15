package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.OrderStatus;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.OrderMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.OrderDTO;
import catholic.ac.kr.secureuserapp.model.dto.OrderItemDTO;
import catholic.ac.kr.secureuserapp.model.dto.OrderRequest;
import catholic.ac.kr.secureuserapp.model.entity.*;
import catholic.ac.kr.secureuserapp.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final ReviewRepository reviewRepository;
    private final NotificationService notificationService;
    private final PointRepository pointRepository;

    @Transactional
    public ApiResponse<OrderDTO> checkout(Long userId, OrderRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());

        if (cartItems.isEmpty()) {
            return ApiResponse.error("Cart is empty");
        }

        BigDecimal total = cartItems.stream()
                .map(item -> {
                    BigDecimal unitPrice = item.getBook().getSalePrice() != null
                            ? item.getBook().getSalePrice()
                            : item.getBook().getPrice();
                    return unitPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal finalTotal = total;
        Coupon coupon = null;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            coupon = couponRepository.findByCouponCode(request.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
            finalTotal = applyCoupon(total, coupon, userId);
        }

        boolean checkRecipientPhone = checkNumberPhoneOrder(request.getRecipientPhone());
        if (!checkRecipientPhone) {
            return ApiResponse.error("Số điện thoại không hợp lệ");
        }

        boolean checkRecipientName = checkRecipientName(request.getRecipientName());
        if (!checkRecipientName) {
            return ApiResponse.error("Tên không hợp lệ");
        }

        boolean checkUsePoint = checkUsePoint(request.getUsePoint() == null ?
                null : request.getUsePoint().toString()); //true
        if (!checkUsePoint) {
            return ApiResponse.error("Point không hợp lệ");
        }

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));

        if (request.getUsePoint() != null && request.getUsePoint().compareTo(BigDecimal.ZERO) > 0) {
            if (point.getPoint().compareTo(request.getUsePoint()) < 0) {
                return ApiResponse.error("Không đủ point");
            }

            finalTotal = applyPoint(finalTotal, request.getUsePoint(), userId);

            point.setPoint(point.getPoint().subtract(request.getUsePoint()));
            point.setUpdatedAt(Timestamp.valueOf(LocalDateTime.now()));
        }

        BigDecimal pointHoard = finalTotal.multiply(BigDecimal.valueOf(0.01)); //cộng 1% giá trị đơn hàng vào điểm tích lũy

        Order order = new Order();
        order.setUser(cart.getUser());
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(finalTotal);
        order.setTotalDiscount(total.subtract(finalTotal));
        order.setPointHoard(pointHoard);
        order.setPointUsage(request.getUsePoint());
        order.setShippingAddress(request.getShippingAddress());
        order.setRecipientName(request.getRecipientName());
        order.setRecipientPhone(request.getRecipientPhone());
        order.setNote(request.getNote());
        order.setConfirmed(false);
        order.setExpiryCancel(LocalDateTime.now().plusDays(7));

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
            BigDecimal purchasePrice = book.getSalePrice() != null ? book.getSalePrice() : book.getPrice(); //kiem tra luc mua la gia giam hay gia goc
            item.setPrice(purchasePrice);
            item.setReviewed(false);

            orderItems.add(item);
        }

        order.setOrderItems(orderItems);
        orderRepository.save(order);
        notificationService.createNotification(order.getUser().getId(), order.getId()); //gửi thông báo PENDING khi đặt hàng thành công

        cartItemRepository.deleteAll(cartItems);

        point.setPoint(point.getPoint().add(pointHoard));
        point.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        pointRepository.save(point);

        return ApiResponse.success("Order successfully", orderMapper.toOrderDTO(order));
    }

    private boolean checkNumberPhoneOrder(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return false;
        }

        for (Character c : phoneNumber.toCharArray()) {
            if (!Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    //regex
    private boolean checkUsePoint(String usePoint) {
        if (usePoint == null || usePoint.isEmpty()) {
            return true;
        }
        return usePoint.matches("\\d+");
    }

    private boolean checkRecipientName(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }

        for (Character c : name.toCharArray()) {
            if (!(Character.isAlphabetic(c) || Character.isSpaceChar(c))) {
                return false;
            }
        }
        return true;
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

        if (!coupon.isUsage() && couponRepository.countUsageCouponByUserId(coupon.getCouponCode(), userId) >= 1) {
            throw new RuntimeException("Coupon already used");
        }

        BigDecimal discount = coupon.isPercentDiscount()
                ? total.multiply(coupon.getDiscountPercent()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : coupon.getDiscountAmount();

        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        if (discount.compareTo(total) > 0)
            return total.subtract(total);

        return total.subtract(discount);
    }


    private BigDecimal applyPoint(BigDecimal total, BigDecimal usePoint, Long userId) {
        Optional<Point> point = pointRepository.findByUserId(userId);

        if (point.isEmpty()) {
            throw new RuntimeException("Chưa có point");
        }

        if (usePoint.compareTo(point.get().getPoint()) > 0) {
            throw new RuntimeException("Không đủ point");
        }

        return total.subtract(usePoint);
    }


    public ApiResponse<Page<OrderDTO>> getOrderByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findByUserId(userId, pageable,false);

        Page<OrderDTO> orderDTOS = orders.map(order -> {
            OrderDTO orderDTO = orderMapper.toOrderDTO(order);
            BigDecimal totalDefaultPrice = BigDecimal.ZERO;
            for (OrderItemDTO itemDTO : orderDTO.getItems()) {
                boolean reviewed = reviewRepository.existsByUserIdAndBookIdAndOrderId(
                        userId,
                        itemDTO.getBookId(),
                        order.getId()
                );

                if (reviewed) {
                    itemDTO.setReviewed(true);
                }
                totalDefaultPrice = totalDefaultPrice.add(
                        BigDecimal.valueOf(itemDTO.getPrice())
                                .multiply(BigDecimal.valueOf(itemDTO.getQuantity())));
                orderDTO.setTotalDefaultPrice(totalDefaultPrice);

            }
            return orderDTO;
        });
        return ApiResponse.success("success", orderDTOS);
    }

    //only ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderDTO>> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderDTO>> getAllOrdersAndFilter(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalPrice").descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderDTO>> getAllOrdersAndFilterASC(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("totalPrice").ascending());
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderDTO>> getAllOrdersAndFilterCreated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        Page<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<OrderDTO>> getAllOrdersAndFilterByStatus(int page, int size, OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        Page<OrderDTO> orderDTOS = orderMapper.toOrderDTO(orders);
        return ApiResponse.success("success", orderDTOS);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderDTO> updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);
        order.setConfirmed(true);
        orderRepository.save(order);
        notificationService.createNotification(order.getUser().getId(), orderId); //gửi thông báo khi cập nhật status cua đơn hàng

        return ApiResponse.success("Updated order status successfully", orderMapper.toOrderDTO(order));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Integer> countOrdersNotConfirmed() {
        int count = orderRepository.countByConfirmed(false);

        return ApiResponse.success("success", count);
    }

    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    @Transactional
    public ApiResponse<String> deleteOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setDeleted(true);
        orderRepository.save(order);

        return ApiResponse.success("Deleted order successfully");
    }

    public ApiResponse<String> cancelOrder(Long userId,Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED){
            return ApiResponse.error("Đơn hàng đã bị hủy");
        }

        else if (order.getStatus() == OrderStatus.SHIPPED) {
            return  ApiResponse.error("Đơn hàng đã giao thành công nên không thể hủy");
        }

        Point point = pointRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Point not found"));
        order.setStatus(OrderStatus.CANCELLED);

        BigDecimal subtractPoint = order.getPointHoard();
        BigDecimal nowPoint = point.getPoint();

        point.setPoint(nowPoint.subtract(subtractPoint));

        pointRepository.save(point);
        orderRepository.save(order);

        return ApiResponse.success("Cancelled order successfully");
    }
}
