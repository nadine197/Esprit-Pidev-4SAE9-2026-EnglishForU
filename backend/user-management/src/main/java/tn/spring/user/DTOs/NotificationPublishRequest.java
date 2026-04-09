package tn.spring.user.DTOs;

import lombok.Builder;
import lombok.Data;
import tn.spring.user.Enums.NotificationType;

@Data
@Builder
public class NotificationPublishRequest {
    private NotificationType type;
    private String title;
    private String message;
    private String link;
    private Long reportId;
}
