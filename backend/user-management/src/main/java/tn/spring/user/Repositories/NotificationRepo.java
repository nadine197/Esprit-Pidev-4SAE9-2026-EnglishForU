package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.Notification;
import tn.spring.user.Models.User;

import java.util.List;
import java.util.Optional;

public interface NotificationRepo extends JpaRepository<Notification, Long> {
    List<Notification> findByRecipientUserOrderByCreatedAtDesc(User recipientUser);
    Optional<Notification> findByIdAndRecipientUser(Long id, User recipientUser);
    long countByRecipientUserAndReadAtIsNull(User recipientUser);
}
