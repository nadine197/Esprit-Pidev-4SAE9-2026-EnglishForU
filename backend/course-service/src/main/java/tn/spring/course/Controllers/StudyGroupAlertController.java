package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.time.Instant;
import java.util.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class StudyGroupAlertController {

    private final SimpMessagingTemplate messagingTemplate;
    private final StudyGroupRepository  studyGroupRepository;

    // ── WebSocket STOMP ───────────────────────────────────────

    @MessageMapping("/study-group/{groupId}/content")
    public void newContent(
            @DestinationVariable Long groupId,
            @Payload Map<String, Object> payload) {

        studyGroupRepository.findById(groupId).ifPresent(group -> {
            Map<String, Object> alert = buildAlert(
                    "NEW_CONTENT", groupId, group.getName(),
                    "Nouveau contenu ajouté dans \""
                            + group.getName() + "\" : "
                            + payload.getOrDefault("title", "")
            );
            alert.put("contentTitle", payload.getOrDefault("title", ""));
            alert.put("contentType",  payload.getOrDefault("type",  ""));
            broadcast(groupId, alert);
        });
    }

    @MessageMapping("/study-group/{groupId}/session")
    public void newSession(
            @DestinationVariable Long groupId,
            @Payload Map<String, Object> payload) {

        studyGroupRepository.findById(groupId).ifPresent(group -> {
            Map<String, Object> alert = buildAlert(
                    "NEW_SESSION", groupId, group.getName(),
                    "Nouvelle session planifiée dans \""
                            + group.getName() + "\" le "
                            + payload.getOrDefault("date", "")
            );
            alert.put("sessionDate", payload.getOrDefault("date", ""));
            broadcast(groupId, alert);
        });
    }

    @MessageMapping("/study-group/{groupId}/message")
    public void newMessage(
            @DestinationVariable Long groupId,
            @Payload Map<String, Object> payload) {

        studyGroupRepository.findById(groupId).ifPresent(group -> {
            Map<String, Object> alert = buildAlert(
                    "NEW_MESSAGE", groupId, group.getName(),
                    payload.getOrDefault("sender", "Quelqu'un")
                            + " a envoyé un message dans \""
                            + group.getName() + "\""
            );
            alert.put("sender", payload.getOrDefault("sender", ""));
            broadcast(groupId, alert);
        });
    }

    // ── Méthode publique appelée depuis ContentService ────────

    public void notifyNewContent(StudyGroup group,
                                 String contentTitle,
                                 String contentType) {
        Map<String, Object> alert = buildAlert(
                "NEW_CONTENT", group.getGroupId(), group.getName(),
                "Nouveau contenu \"" + contentTitle
                        + "\" ajouté dans \"" + group.getName() + "\""
        );
        alert.put("contentTitle", contentTitle);
        alert.put("contentType",  contentType);
        broadcast(group.getGroupId(), alert);
        log.info("NEW_CONTENT notifié → groupe {}", group.getGroupId());
    }

    // ── Helpers ───────────────────────────────────────────────

    public Map<String, Object> buildAlert(String type, Long groupId,
                                          String groupName, String message) {
        Map<String, Object> alert = new LinkedHashMap<>();
        alert.put("type",      type);
        alert.put("groupId",   groupId);
        alert.put("groupName", groupName);
        alert.put("message",   message);
        alert.put("timestamp", Instant.now().toString());
        return alert;
    }

    public void broadcast(Long groupId, Map<String, Object> alert) {

        messagingTemplate.convertAndSend(
                "/topic/study-group/" + groupId + "/alerts",
                (Object) alert
        );
        messagingTemplate.convertAndSend(
                "/topic/study-groups/notifications",
                (Object) alert
        );
    }
}