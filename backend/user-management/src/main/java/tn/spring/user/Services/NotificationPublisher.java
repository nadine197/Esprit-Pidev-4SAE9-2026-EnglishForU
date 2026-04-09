package tn.spring.user.Services;

import tn.spring.user.DTOs.NotificationPublishRequest;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.User;

import java.util.List;
import java.util.UUID;

public interface NotificationPublisher {
    long publishToRole(UserRole role, NotificationPublishRequest request);

    long publishToUser(UUID recipientUserId, NotificationPublishRequest request);

    long publishToUsers(List<User> recipients, NotificationPublishRequest request);
}
