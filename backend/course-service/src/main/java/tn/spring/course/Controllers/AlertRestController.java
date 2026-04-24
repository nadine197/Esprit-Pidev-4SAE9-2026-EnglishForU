package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/study-groups")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor
public class AlertRestController {

    private final StudyGroupRepository     studyGroupRepository;
    private final StudyGroupAlertController alertController;

    // POST /api/study-groups/{groupId}/alert/content
    @PostMapping("/{groupId}/alert/content")
    public ResponseEntity<Void> alertContent(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> body) {

        studyGroupRepository.findById(groupId).ifPresent(group ->
                alertController.notifyNewContent(
                        group,
                        body.getOrDefault("title", "Nouveau contenu"),
                        body.getOrDefault("type",  "PDF")
                )
        );
        return ResponseEntity.ok().build();
    }

    // POST /api/study-groups/{groupId}/alert/session
    @PostMapping("/{groupId}/alert/session")
    public ResponseEntity<Void> alertSession(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> body) {

        studyGroupRepository.findById(groupId).ifPresent(group -> {
            Map<String, Object> alert = alertController.buildAlert(
                    "NEW_SESSION", groupId, group.getName(),
                    "Nouvelle session le " + body.getOrDefault("date", "")
            );
            alert.put("sessionDate", body.getOrDefault("date", ""));
            alertController.broadcast(groupId, alert);
        });
        return ResponseEntity.ok().build();
    }

    // POST /api/study-groups/{groupId}/alert/message
    @PostMapping("/{groupId}/alert/message")
    public ResponseEntity<Void> alertMessage(
            @PathVariable Long groupId,
            @RequestBody Map<String, String> body) {

        studyGroupRepository.findById(groupId).ifPresent(group -> {
            Map<String, Object> alert = alertController.buildAlert(
                    "NEW_MESSAGE", groupId, group.getName(),
                    body.getOrDefault("sender", "Quelqu'un")
                            + " a envoyé un message dans \""
                            + group.getName() + "\""
            );
            alert.put("sender", body.getOrDefault("sender", ""));
            alertController.broadcast(groupId, alert);
        });
        return ResponseEntity.ok().build();
    }
}