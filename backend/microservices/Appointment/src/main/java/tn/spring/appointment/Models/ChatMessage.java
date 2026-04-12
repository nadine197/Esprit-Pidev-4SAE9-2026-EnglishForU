package tn.spring.appointment.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String groupId;   // ID du DiscussionGroup
    private String senderId;  // ID de l'utilisateur
    private String senderName;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;

    // Pour la réponse : on stocke l'ID et un aperçu du message parent
    private UUID replyToId;
    private String replyToText;
    private String replyToUser;

    // Pour les réactions : on stocke une Map <EmailUtilisateur, Emoji>
    private String reaction; // Contiendra l'emoji (ex: "❤️")
    private boolean isPinned; // Par défaut false


}
