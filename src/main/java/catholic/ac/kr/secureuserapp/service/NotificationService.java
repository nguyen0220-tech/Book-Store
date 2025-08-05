package catholic.ac.kr.secureuserapp.service;

import catholic.ac.kr.secureuserapp.Status.FriendStatus;
import catholic.ac.kr.secureuserapp.Status.NotificationType;
import catholic.ac.kr.secureuserapp.Status.OrderStatus;
import catholic.ac.kr.secureuserapp.Status.Sex;
import catholic.ac.kr.secureuserapp.exception.ResourceNotFoundException;
import catholic.ac.kr.secureuserapp.mapper.NotificationMapper;
import catholic.ac.kr.secureuserapp.model.dto.ApiResponse;
import catholic.ac.kr.secureuserapp.model.dto.NotificationDTO;
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

import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookMarkRepository bookMarkRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;
    private final CouponRepository couponRepository;
    private final FriendRepository friendRepository;

    public ApiResponse<Page<NotificationDTO>> getAllNotifications(Long userId, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Notification> notificationList = notificationRepository.findByUserId(userId, pageRequest);
        Page<NotificationDTO> dtoList = notificationList.map(NotificationMapper::toNotificationDTO);

        return ApiResponse.success("Notification list", dtoList);
    }

    public ApiResponse<NotificationDTO> getNotification(Long userId, Long notificationId) {
        Optional<Notification> notificationOptional = notificationRepository.findByUserIdAndId(userId, notificationId);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            notification.setRead(true);
            notificationRepository.save(notification);
            NotificationDTO dto = NotificationMapper.toNotificationDTO(notification);
            return ApiResponse.success("Notification", dto);
        }

        return ApiResponse.error("Notification not found");
    }

    public ApiResponse<Page<NotificationDTO>> getNotificationByUserIdAnFilterType(
            Long userId, NotificationType type, int page, int siz) {
        Pageable pageable = PageRequest.of(page, siz, Sort.by("createdAt").descending());
        Page<Notification> notificationList = notificationRepository.findByUserIdAndType(userId, type, pageable);
        Page<NotificationDTO> dtoList = notificationList.map(NotificationMapper::toNotificationDTO);

        return ApiResponse.success("Filter notification with type: " + type, dtoList);

    }

    public ApiResponse<NotificationDTO> createNotification(Long userId, Long orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus status = order.getStatus();

        boolean alreadyNotified = notificationRepository.exitsByOrderAndMessage(order, generateMessage(status));


        if (!alreadyNotified) {
            Notification notification = new Notification();

            notification.setUser(user);
            notification.setOrder(order);
            notification.setBook(null);
            notification.setMessage(generateMessage(status));
            notification.setRead(false);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            notification.setType(NotificationType.ORDER);

            notificationRepository.save(notification);

            return ApiResponse.success("Notification", NotificationMapper.toNotificationDTO(notification));
        }

        return ApiResponse.success("Order status is not eligible for notification.");
    }

    private String generateMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Order pending";
            case PAID -> "Order paid";
            case SHIPPED -> "Order shipped";
            case CANCELLED -> "Order cancelled";
        };
    }

    public void createBookMarkDiscountNotification(Long userId, Book book) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isBookMarked = bookMarkRepository.existsByUserAndBook(user, book);

        if (!isBookMarked) {
            ApiResponse.success("Notification already exists. No new notification sent.");
            return;
        }

        Category category = categoryRepository.findByBookId(book.getId());

        Sex sex = user.getSex();
        String call;
        if (sex == Sex.MALE) {
            call = "Mr.";
        } else if (sex == Sex.FEMALE) {
            call = "Mrs.";
        } else call = "";

        String message;

        if (user.getFullName() == null || user.getFullName().isEmpty()) {
            message = String.format("[%s %s] %s - %s : Big sale %.2f -> %.2f won",
                    call, user.getUsername(), category.getName(), book.getTitle(), book.getPrice(), book.getSalePrice());
        } else
            message = String.format("[%s %s] %s - %s : Big sale %.2f -> %.2f won",
                    call, user.getFullName(), category.getName(), book.getTitle(), book.getPrice(), book.getSalePrice());


        boolean alreadyNotified = notificationRepository.existsByBookAndMessage(user, book, message);

        if (!alreadyNotified) {
            Notification notification = new Notification();

            notification.setUser(user);
            notification.setOrder(null);
            notification.setBook(book);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            notification.setType(NotificationType.BOOK_DISCOUNT);

            notificationRepository.save(notification);

            ApiResponse.success("Notification", NotificationMapper.toNotificationDTO(notification));
            return;
        }
        ApiResponse.success("Book mark discount is not eligible for notification.");
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> createSystemNotification(String message) {
        List<User> users = userRepository.findAll();
        List<Notification> notifications = new ArrayList<>();

        for (User user : users) {
            notifications.add(Notification.builder()
                    .user(user)
                    .order(null)
                    .book(null)
                    .message(message)
                    .read(false)
                    .createdAt(new Timestamp(System.currentTimeMillis()))
                    .type(NotificationType.SYSTEM)
                    .build());
        }

        notificationRepository.saveAll(notifications);

        Map<String, Object> result = new HashMap<>();
        result.put("message", message);
        result.put("sendCount", notifications.size());

        return ApiResponse.success("Created and send system notification: ", result);
    }

    public void createCouponExpiredNotification() {
        LocalDateTime now = LocalDateTime.now();
        List<Coupon> expiredCouponOfOneDayLeft = couponRepository.findAll().stream()
                .filter(coupon -> {
                    long hourLeft = Duration.between(now, coupon.getExpired()).toHours(); //.toHours():Lấy tổng số giờ của khoảng thời gian Duration vừa tính được
                    return hourLeft <= 24 && hourLeft > 0 && coupon.isActive();
                }).toList();

        List<Notification> notifications = new ArrayList<>();
        for (Coupon coupon : expiredCouponOfOneDayLeft) {
            for (User user : coupon.getUsers()) {
                notifications.add(Notification.builder()
                        .user(user)
                        .book(null)
                        .order(null)
                        .createdAt(new Timestamp(System.currentTimeMillis()))
                        .read(false)
                        .message("Coupon " + coupon.getCouponCode() + " sẽ hết hạn sau 24h")
                        .type(NotificationType.COUPON)
                        .build());
            }
        }
        notificationRepository.saveAll(notifications);
    }

    public void createRequestFriendNotification(Long userId, Long friendId) {
        User user = userRepository.findById(friendId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Friend friend = friendRepository.findByUserIdAndFriendIdAndStatus(userId, friendId, FriendStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Friend not found"));

        Notification notification = Notification.builder()
                .user(user)
                .order(null)
                .book(null)
                .message(friend.getUser().getUsername() + " dã gửi lời mời kết bạn")
                .read(false)
                .createdAt(new Timestamp(System.currentTimeMillis()))
                .type(NotificationType.FRIEND)
                .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public ApiResponse<String> markNotificationAsRead(Long userId, Long notificationId) {
        if (notificationRepository.markAsRead(userId, notificationId) == 0) {
            throw new ResourceNotFoundException("Notification not found");
        }

        return ApiResponse.success("Notification marked as read");
    }

    public ApiResponse<Integer> countNotificationUnread(Long userId) {
        int count = notificationRepository.countUnreadNotificationsByUserId(userId);
        if (count == 0) {
            return ApiResponse.success("All notifications read");
        }
        return ApiResponse.success("notification unread", count);
    }

    @Transactional
    public ApiResponse<String> deleteNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notificationRepository.delete(notification);
        return ApiResponse.success("Notification deleted");
    }
}