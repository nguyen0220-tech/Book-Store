package catholic.ac.kr.secureuserapp.mapper;

import catholic.ac.kr.secureuserapp.model.dto.NotificationDTO;
import catholic.ac.kr.secureuserapp.model.entity.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationMapper {
    public static NotificationDTO toNotificationDTO(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setTitle(notification.getBook() != null ? notification.getBook().getTitle() : null);
        dto.setOrderId(notification.getOrder() != null ? notification.getOrder().getId() : null);
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setType(notification.getType().name());

        return dto;
    }

    public static List<NotificationDTO> toNotificationDTOList(List<Notification> notifications) {
        if (notifications == null) {
            return null;
        }

        List<NotificationDTO> dtoList = new ArrayList<>();

        for (Notification notification : notifications) {
            dtoList.add(toNotificationDTO(notification));
        }
        return dtoList;
    }
}
