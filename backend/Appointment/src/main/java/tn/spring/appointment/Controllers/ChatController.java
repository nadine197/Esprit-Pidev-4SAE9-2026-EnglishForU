package tn.spring.appointment.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import tn.spring.appointment.Models.ChatMessage;
import tn.spring.appointment.Repositories.ChatMessageRepository; // Import déjà présent normalement
import tn.spring.appointment.Services.ChatService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/discussions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    // --- IL MANQUAIT CETTE LIGNE ---
    private final ChatMessageRepository chatMessageRepository;

    // Envoyer un message
    @MessageMapping("/chat.send/{groupId}")
    public void sendMessage(@DestinationVariable String groupId, @Payload ChatMessage chatMessage) {
        ChatMessage savedMsg = chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, savedMsg);
    }

    // Charger l'historique (REST)
    @GetMapping("/groups/{groupId}/messages")
    public List<ChatMessage> getHistory(@PathVariable String groupId) {
        return chatService.getMessagesByGroupId(groupId);
    }

    // MODIFIER un message
    @MessageMapping("/chat.edit/{groupId}")
    public void editMessage(@DestinationVariable String groupId, @Payload ChatMessage chatMessage) {
        chatService.saveMessage(chatMessage);
        messagingTemplate.convertAndSend("/topic/group/" + groupId, chatMessage);
    }

    // SUPPRIMER un message
    @MessageMapping("/chat.delete/{groupId}")
    public void deleteMessage(@DestinationVariable String groupId, @Payload ChatMessage chatMessage) {
        // Maintenant cette ligne fonctionne car chatMessageRepository est injecté
        chatMessageRepository.deleteById(chatMessage.getId());

        // Signal spécial pour que le front retire le message de la liste
        chatMessage.setContent("DELETED_SIGNAL");
        messagingTemplate.convertAndSend("/topic/group/" + groupId, chatMessage);
    }

    @MessageMapping("/chat.typing/{groupId}")
    public void processTyping(@DestinationVariable String groupId, Map<String, Object> payload) {
        // On ajoute (Object) devant payload pour lever l'ambiguïté
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/typing", (Object) payload);
    }

    @MessageMapping("/chat.pin/{groupId}")
    public void pinMessage(@DestinationVariable String groupId, @Payload ChatMessage chatMessage) {
        // 1. (Optionnel) Si tu veux un seul message épinglé à la fois :
        // Tu pourrais faire une requête pour mettre isPinned=false pour tout le groupe d'abord.

        // 2. Sauvegarder le statut
        chatService.saveMessage(chatMessage);

        // 3. Diffuser
        messagingTemplate.convertAndSend("/topic/group/" + groupId, chatMessage);
    }
}