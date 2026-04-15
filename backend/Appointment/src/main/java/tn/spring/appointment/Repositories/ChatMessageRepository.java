package tn.spring.appointment.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.appointment.Models.ChatMessage;
import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    // Récupère tous les messages d'un groupe spécifique dans l'ordre chronologique
    List<ChatMessage> findByGroupIdOrderByTimestampAsc(String groupId);
}