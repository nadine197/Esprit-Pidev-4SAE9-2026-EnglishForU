package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.NotificationResponse;
import tn.spring.user.Models.Notification;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.NotificationRepo;
import tn.spring.user.Repositories.UserRepos;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepo notificationRepo;
    private final UserRepos userRepos;

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);

        return notificationRepo.findByRecipientUserOrderByCreatedAtDesc(currentUser)
                .stream()
                .map(this::toNotificationResponse)
                .toList();
    }

    public NotificationResponse markAsRead(Long notificationId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);

        Notification notification = notificationRepo.findByIdAndRecipientUser(notificationId, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "NOTIFICATION_NOT_FOUND"));

        if (notification.getReadAt() == null) {
            notification.setReadAt(Instant.now());
            notification = notificationRepo.save(notification);
        }

        return toNotificationResponse(notification);
    }

    private User findUserByEmail(String email) {
        return userRepos.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
    }

    private NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .link(notification.getLink())
                .reportId(notification.getReport() != null ? notification.getReport().getId() : null)
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .read(notification.getReadAt() != null)
                .build();
    }
}
