package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.Controllers.StudyGroupAlertController;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Models.StudyGroupStatus;
import tn.spring.course.Repositories.StudyGroupRepository;
import tn.spring.course.Services.StudyGroupNotificationService;
import tn.spring.course.Services.StudyGroupService;
import tn.spring.course.Services.StudyGroupSchedulerService;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-groups")
@CrossOrigin(origins = "http://localhost:4200")
@RequiredArgsConstructor

public class StudyGroupController {

    private final StudyGroupService             studyGroupService;
    private final StudyGroupRepository          studyGroupRepository;
    private final StudyGroupNotificationService notificationService; // alertes système
    private final StudyGroupAlertController     alertController;     // alertes métier
    private final StudyGroupSchedulerService schedulerService;
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TUTOR')")
    public StudyGroupResponseDTO createStudyGroup(
            @RequestBody StudyGroupRequestDTO dto) {
        return studyGroupService.createStudyGroup(dto);
    }

    @GetMapping
    public List<StudyGroupResponseDTO> getAllStudyGroups() {
        return studyGroupService.getAllStudyGroups();
    }

    @GetMapping("/{id}")
    public StudyGroupResponseDTO getStudyGroup(@PathVariable Long id) {
        return studyGroupService.getStudyGroup(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TUTOR')")
    public StudyGroupResponseDTO updateStudyGroup(
            @PathVariable Long id,
            @RequestBody StudyGroupRequestDTO dto) {

        // Statut AVANT modification
        StudyGroupResponseDTO before = studyGroupService.getStudyGroup(id);

        // Mise à jour
        StudyGroupResponseDTO updated = studyGroupService.updateStudyGroup(id, dto);

        // ✅ Notifier si statut → CANCELLED
        if (dto.getStatus() == StudyGroupStatus.CANCELLED
                && before.getStatus() != StudyGroupStatus.CANCELLED) {
            studyGroupRepository.findById(id)
                    .ifPresent(notificationService::notifyGroupCancelled);
        }

        return updated;
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteStudyGroup(@PathVariable Long id) {
        studyGroupService.deleteStudyGroup(id);
    }

    // ── Calendrier ────────────────────────────────────────────

    @GetMapping("/calendar/by-date")
    public ResponseEntity<List<StudyGroupResponseDTO>> byDate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        return ResponseEntity.ok(studyGroupService.getGroupsByDate(date));
    }

    @GetMapping("/calendar/by-month")
    public ResponseEntity<List<StudyGroupResponseDTO>> byMonth(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(studyGroupService.getGroupsByMonth(year, month));
    }

    @GetMapping("/calendar/marked-dates")
    public ResponseEntity<Map<String, List<String>>> markedDates(
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(studyGroupService.getMarkedDates(year, month));
    }

    // ── Stats ─────────────────────────────────────────────────

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(studyGroupService.getStats());
    }

    // ── Recherche ─────────────────────────────────────────────

    @GetMapping("/search")
    public ResponseEntity<List<StudyGroupResponseDTO>> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Integer courseId) {
        return ResponseEntity.ok(
                studyGroupService.searchGroups(name, level, status, location, courseId)
        );
    }

    // ── Audit ─────────────────────────────────────────────────

    @GetMapping("/{id}/audit")
    public ResponseEntity<List<Map<String, Object>>> getAuditLog(
            @PathVariable Long id) {
        return ResponseEntity.ok(studyGroupService.getAuditLog(id));
    }
    @PostMapping("/scheduler/run")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> runScheduler() {
        schedulerService.updateStatuses();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("message",   "Statuts mis à jour avec succès");
        result.put("timestamp", new Date().toString());
        return ResponseEntity.ok(result);
    }
    @PostMapping("/chatbot")
    public ResponseEntity<Map<String, String>> chat(
            @RequestParam String message,
            @RequestParam(required = false) Long groupId) {
        String reply = studyGroupService.chat(message, groupId);
        return ResponseEntity.ok(Map.of("reply", reply));
    }
    @PostMapping("/validated")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN', 'TUTOR')")
    public ResponseEntity<StudyGroupResponseDTO> createWithValidation(
            @RequestBody StudyGroupRequestDTO dto) {
        return ResponseEntity.ok(
                studyGroupService.createStudyGroupWithValidation(dto));
    }

}