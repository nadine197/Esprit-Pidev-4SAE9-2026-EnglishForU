package tn.spring.appointment.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.appointment.Models.ChatMessage;
import tn.spring.appointment.Repositories.ChatMessageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;

    /**
     * Sauvegarde un nouveau message
     */
    public ChatMessage saveMessage(ChatMessage message) {
        // On s'assure que le timestamp est généré au moment de l'enregistrement
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        return chatMessageRepository.save(message);
    }

    /**
     * Récupère l'historique d'un groupe
     */
    public List<ChatMessage> getMessagesByGroupId(String groupId) {
        return chatMessageRepository.findByGroupIdOrderByTimestampAsc(groupId);
    }
}