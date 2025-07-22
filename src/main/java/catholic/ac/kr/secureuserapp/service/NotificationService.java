package catholic.ac.kr.secureuserapp.service;

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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookMarkRepository bookMarkRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;

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

    public ApiResponse<NotificationDTO> createBookMarkDiscountNotification(Long userId, Book book) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isBookMarked = bookMarkRepository.existsByUserAndBook(user, book);

        if (!isBookMarked) {
            return ApiResponse.success("Notification already exists. No new notification sent.");
        }

        Category category = categoryRepository.findByBookId(book.getId());

        Sex sex = user.getSex();
        String call;
        if (sex == Sex.MALE) {
            call = "Mr";
        } else if (sex == Sex.FEMALE) {
            call = "Mrs";
        } else call = "";


        String message = String.format("[%s %s] %s - %s : big sale %.2f -> %.2f won",
                call,user.getUsername(), category.getName(), book.getTitle(), book.getPrice(), book.getSalePrice());

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

            return ApiResponse.success("Notification", NotificationMapper.toNotificationDTO(notification));
        }
        return ApiResponse.success("Book mark discount is not eligible for notification.");
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
}

/*
xu li:chi khi thay doi gia sale moi gui thong bao
loi hien thi
 📩 Book One Piece : big sale 159000.00 -> 0.00 won
📦 Đơn hàng #null
🕒 03:42:17 22/7/2025
 */
