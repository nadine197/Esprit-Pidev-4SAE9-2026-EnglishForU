package tn.spring.course.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyGroupNotificationService {

    private final StudyGroupRepository  studyGroupRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void notifyAlmostFull() {
        studyGroupRepository.findAll().stream()
                .filter(g -> g.getStudentsIds() != null && g.getMaxCapacity() > 0)
                .filter(g -> {
                    double rate = (double) g.getStudentsIds().size() / g.getMaxCapacity();
                    return rate >= 0.8 && rate < 1.0;
                })
                .forEach(g -> sendWebSocket(g.getGroupId(), "ALMOST_FULL",
                        "Le groupe \"" + g.getName() + "\" est presque plein ("
                                + g.getStudentsIds().size() + "/" + g.getMaxCapacity() + ")."));
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void notifyFull() {
        studyGroupRepository.findAll().stream()
                .filter(g -> g.getStudentsIds() != null
                        && g.getMaxCapacity() > 0
                        && g.getStudentsIds().size() >= g.getMaxCapacity())
                .forEach(g -> sendWebSocket(g.getGroupId(), "FULL",
                        "Le groupe \"" + g.getName() + "\" est complet !"));
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notifyStartingTomorrow() {
        studyGroupRepository.findByStartdate(getTomorrow()).forEach(g ->
                sendWebSocket(g.getGroupId(), "STARTING_TOMORROW",
                        "Le groupe \"" + g.getName()
                                + "\" commence demain à " + g.getLocation() + "."));
    }

    @Scheduled(cron = "0 0 20 * * *")
    public void notifyEnded() {
        studyGroupRepository.findByEnddate(getToday()).forEach(g ->
                sendWebSocket(g.getGroupId(), "ENDED",
                        "Le groupe \"" + g.getName() + "\" se termine aujourd'hui."));
    }

    public void notifyGroupCancelled(StudyGroup g) {
        sendWebSocket(g.getGroupId(), "CANCELLED",
                "Le groupe \"" + g.getName() + "\" a été annulé.");
    }

    public void notifyNewMember(StudyGroup g, UUID studentId) {
        int remaining = g.getMaxCapacity() - g.getStudentsIds().size();
        sendWebSocket(g.getGroupId(), "NEW_MEMBER",
                "Nouveau membre dans \"" + g.getName()
                        + "\". Places restantes : " + remaining + ".");
    }

    private void sendWebSocket(Long groupId, String type, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("groupId",   groupId);
        payload.put("type",      type);
        payload.put("message",   message);
        payload.put("timestamp", new Date().toString());

        // ✅ cast (Object) pour lever l'ambiguïté
        messagingTemplate.convertAndSend(
                "/topic/study-groups/" + groupId + "/notifications",
                (Object) payload
        );
        messagingTemplate.convertAndSend(
                "/topic/study-groups/notifications",
                (Object) payload
        );

        log.info("Notification {} → groupe {}", type, groupId);
    }

    private Date getToday() {
        return Date.from(LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date getTomorrow() {
        return Date.from(LocalDate.now().plusDays(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void sendWebSocketPublic(Long groupId, String type, String message) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("groupId",   groupId);
        payload.put("type",      type);
        payload.put("message",   message);
        payload.put("timestamp", new Date().toString());

        messagingTemplate.convertAndSend(
                "/topic/study-groups/" + groupId + "/notifications",
                (Object) payload
        );

        messagingTemplate.convertAndSend(
                "/topic/study-groups/notifications",
                (Object) payload
        );

        log.info("Notification {} → groupe {}", type, groupId);
    }
}
