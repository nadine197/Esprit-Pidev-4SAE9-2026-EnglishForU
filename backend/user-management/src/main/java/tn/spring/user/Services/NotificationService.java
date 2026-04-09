package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.NotificationPublishRequest;
import tn.spring.user.DTOs.NotificationResponse;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Notification;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.NotificationRepo;
import tn.spring.user.Repositories.ReportRepo;
import tn.spring.user.Repositories.UserRepos;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService implements NotificationPublisher {

    private final NotificationRepo notificationRepo;
    private final UserRepos userRepos;
    private final ReportRepo reportRepo;
    private final SimpMessagingTemplate messagingTemplate;

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

    @Transactional(readOnly = true)
    public long getUnreadCount(String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        return notificationRepo.countByRecipientUserAndReadAtIsNull(currentUser);
    }

    @Override
    public long publishToRole(UserRole role, NotificationPublishRequest request) {
        List<User> recipients = userRepos.findByRole(role);
        return publishToUsers(recipients, request);
    }

    @Override
    public long publishToUser(UUID recipientUserId, NotificationPublishRequest request) {
        User recipient = userRepos.findById(recipientUserId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "RECIPIENT_NOT_FOUND"));
        return publishToUsers(List.of(recipient), request);
    }

    @Override
    public long publishToUsers(List<User> recipients, NotificationPublishRequest request) {
        if (recipients == null || recipients.isEmpty()) {
            return 0;
        }

        NotificationPublishRequest validatedRequest = validatePublishRequest(request);
        Report report = resolveReportIfPresent(validatedRequest.getReportId());

        List<Notification> notifications = recipients.stream()
                .map(recipient -> buildNotification(recipient, validatedRequest, report))
                .toList();

        List<Notification> savedNotifications = notificationRepo.saveAll(notifications);
        publishRealtimeNotifications(savedNotifications);
        return savedNotifications.size();
    }

    private User findUserByEmail(String email) {
        return userRepos.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
    }

    private NotificationPublishRequest validatePublishRequest(NotificationPublishRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOTIFICATION_REQUEST_REQUIRED");
        }
        if (request.getType() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOTIFICATION_TYPE_REQUIRED");
        }
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOTIFICATION_TITLE_REQUIRED");
        }
        if (request.getMessage() == null || request.getMessage().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOTIFICATION_MESSAGE_REQUIRED");
        }
        if (request.getLink() == null || request.getLink().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "NOTIFICATION_LINK_REQUIRED");
        }

        return NotificationPublishRequest.builder()
                .type(request.getType())
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .link(request.getLink().trim())
                .reportId(request.getReportId())
                .build();
    }

    private Report resolveReportIfPresent(Long reportId) {
        if (reportId == null) {
            return null;
        }

        return reportRepo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "REPORT_NOT_FOUND"));
    }

    private Notification buildNotification(User recipient, NotificationPublishRequest request, Report report) {
        return Notification.builder()
                .recipientUser(recipient)
                .report(report)
                .type(request.getType())
                .title(request.getTitle())
                .message(request.getMessage())
                .link(request.getLink())
                .build();
    }

    private void publishRealtimeNotifications(List<Notification> notifications) {
        for (Notification notification : notifications) {
            NotificationResponse payload = toNotificationResponse(notification);
            String recipientTopic = "/topic/notifications/" + notification.getRecipientUser().getId();
            messagingTemplate.convertAndSend(recipientTopic, payload);

            if (payload.getReportId() != null) {
                messagingTemplate.convertAndSend("/topic/helpdesk/reports", payload);
            }
        }
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
