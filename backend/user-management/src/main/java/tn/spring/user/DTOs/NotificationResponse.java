package tn.spring.user.DTOs;

import lombok.Builder;
import lombok.Data;
import tn.spring.user.Enums.NotificationType;

import java.time.Instant;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String link;
    private Long reportId;
    private Instant createdAt;
    private Instant readAt;
    private boolean read;
}
