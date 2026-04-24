package tn.spring.course.Services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
import tn.spring.course.Repositories.StudyGroupRepository;
import tn.spring.course.Services.StudyGroupNotificationService;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyGroupSchedulerService {

    private final StudyGroupRepository       studyGroupRepository;
    private final StudyGroupNotificationService notificationService;

    // ── Vérifie chaque jour à minuit ─────────────────────────
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateStatuses() {
        Date now = new Date();

        activatePlannedGroups(now);
        completeActiveGroups(now);
    }

    // ── PLANNED → ACTIVE ─────────────────────────────────────
    private void activatePlannedGroups(Date now) {
        List<StudyGroup> toActivate =
                studyGroupRepository.findPlannedToActivate(now);

        toActivate.forEach(group -> {
            group.setStatus(StudyGroupStatus.ACTIVE);
            studyGroupRepository.save(group);

            // ✅ Notifier les membres via WebSocket
            notificationService.sendWebSocketPublic(
                    group.getGroupId(),
                    "STATUS_CHANGED",
                    "Le groupe \"" + group.getName()
                            + "\" est maintenant ACTIF !"
            );

            log.info("Groupe {} → ACTIVE", group.getGroupId());
        });

        log.info("{} groupe(s) passé(s) à ACTIVE", toActivate.size());
    }

    // ── ACTIVE → COMPLETED ───────────────────────────────────
    private void completeActiveGroups(Date now) {
        List<StudyGroup> toComplete =
                studyGroupRepository.findActiveToComplete(now);

        toComplete.forEach(group -> {
            group.setStatus(StudyGroupStatus.COMPLETED);
            studyGroupRepository.save(group);

            // ✅ Notifier les membres via WebSocket
            notificationService.sendWebSocketPublic(
                    group.getGroupId(),
                    "STATUS_CHANGED",
                    "Le groupe \"" + group.getName()
                            + "\" est maintenant TERMINÉ."
            );

            log.info("Groupe {} → COMPLETED", group.getGroupId());
        });

        log.info("{} groupe(s) passé(s) à COMPLETED", toComplete.size());
    }
}