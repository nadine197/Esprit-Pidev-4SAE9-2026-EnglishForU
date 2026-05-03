package tn.spring.course.Controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
import tn.spring.course.Repositories.StudyGroupRepository;
import tn.spring.course.Services.StudyGroupNotificationService;
import tn.spring.course.Services.StudyGroupSchedulerService;
import tn.spring.course.Services.StudyGroupService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGroupControllerIntegrationTest {

    @Mock
    private StudyGroupService studyGroupService;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudyGroupNotificationService notificationService;

    @Mock
    private StudyGroupAlertController alertController;

    @Mock
    private StudyGroupSchedulerService schedulerService;

    @InjectMocks
    private StudyGroupController studyGroupController;

    // ── Test 1 : GET tous les groupes ─────────────────────────
    @Test
    void shouldGetAllStudyGroups() {
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setGroupId(1L);
        dto.setName("Groupe A");
        dto.setLevel("BEGINNER");
        dto.setStatus(StudyGroupStatus.ACTIVE);

        when(studyGroupService.getAllStudyGroups())
                .thenReturn(List.of(dto));

        List<StudyGroupResponseDTO> result =
                studyGroupController.getAllStudyGroups();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Groupe A", result.get(0).getName());
        verify(studyGroupService, times(1)).getAllStudyGroups();
    }

    // ── Test 2 : GET groupe par ID ────────────────────────────
    @Test
    void shouldGetStudyGroupById() {
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setGroupId(1L);
        dto.setName("Groupe B");
        dto.setStatus(StudyGroupStatus.PLANNED);

        when(studyGroupService.getStudyGroup(1L)).thenReturn(dto);

        StudyGroupResponseDTO result =
                studyGroupController.getStudyGroup(1L);

        assertNotNull(result);
        assertEquals("Groupe B", result.getName());
        verify(studyGroupService, times(1)).getStudyGroup(1L);
    }

    // ── Test 3 : GET groupe introuvable ───────────────────────
    @Test
    void shouldThrowWhenGroupNotFound() {
        when(studyGroupService.getStudyGroup(99L))
                .thenThrow(new RuntimeException("StudyGroup not found"));

        assertThrows(RuntimeException.class, () ->
                studyGroupController.getStudyGroup(99L)
        );
    }

    // ── Test 4 : POST créer un groupe ─────────────────────────
    @Test
    void shouldCreateStudyGroup() {
        StudyGroupRequestDTO request = new StudyGroupRequestDTO();
        request.setName("Nouveau Groupe");
        request.setLevel("BEGINNER");
        request.setMaxCapacity(20);
        request.setCourseId(1);
        request.setStatus(StudyGroupStatus.PLANNED);
        request.setTutorId(UUID.randomUUID());
        request.setStudentsIds(new ArrayList<>());

        StudyGroupResponseDTO response = new StudyGroupResponseDTO();
        response.setGroupId(1L);
        response.setName("Nouveau Groupe");
        response.setStatus(StudyGroupStatus.PLANNED);

        when(studyGroupService.createStudyGroup(any()))
                .thenReturn(response);

        StudyGroupResponseDTO result =
                studyGroupController.createStudyGroup(request);

        assertNotNull(result);
        assertEquals("Nouveau Groupe", result.getName());
        verify(studyGroupService, times(1)).createStudyGroup(any());
    }

    // ── Test 5 : PUT modifier un groupe ──────────────────────
    @Test
    void shouldUpdateStudyGroup() {
        StudyGroupRequestDTO request = new StudyGroupRequestDTO();
        request.setName("Groupe Modifié");
        request.setLevel("ADVANCED");
        request.setMaxCapacity(15);
        request.setCourseId(1);
        request.setStatus(StudyGroupStatus.ACTIVE);
        request.setTutorId(UUID.randomUUID());
        request.setStudentsIds(new ArrayList<>());

        StudyGroupResponseDTO before = new StudyGroupResponseDTO();
        before.setGroupId(1L);
        before.setStatus(StudyGroupStatus.PLANNED);

        StudyGroupResponseDTO updated = new StudyGroupResponseDTO();
        updated.setGroupId(1L);
        updated.setName("Groupe Modifié");
        updated.setStatus(StudyGroupStatus.ACTIVE);

        when(studyGroupService.getStudyGroup(1L)).thenReturn(before);
        when(studyGroupService.updateStudyGroup(eq(1L), any()))
                .thenReturn(updated);

        StudyGroupResponseDTO result =
                studyGroupController.updateStudyGroup(1L, request);

        assertNotNull(result);
        assertEquals("Groupe Modifié", result.getName());
        verify(studyGroupService, times(1)).updateStudyGroup(eq(1L), any());
    }

    // ── Test 6 : PUT statut CANCELLED → notification ──────────
    @Test
    void shouldNotifyWhenGroupCancelled() {
        StudyGroupRequestDTO request = new StudyGroupRequestDTO();
        request.setName("Groupe Annulé");
        request.setLevel("BEGINNER");
        request.setMaxCapacity(10);
        request.setCourseId(1);
        request.setStatus(StudyGroupStatus.CANCELLED);
        request.setTutorId(UUID.randomUUID());
        request.setStudentsIds(new ArrayList<>());

        StudyGroupResponseDTO before = new StudyGroupResponseDTO();
        before.setGroupId(1L);
        before.setStatus(StudyGroupStatus.ACTIVE);

        StudyGroupResponseDTO updated = new StudyGroupResponseDTO();
        updated.setGroupId(1L);
        updated.setStatus(StudyGroupStatus.CANCELLED);

        StudyGroup group = new StudyGroup();
        group.setGroupId(1L);

        when(studyGroupService.getStudyGroup(1L)).thenReturn(before);
        when(studyGroupService.updateStudyGroup(eq(1L), any()))
                .thenReturn(updated);
        when(studyGroupRepository.findById(1L))
                .thenReturn(Optional.of(group));

        studyGroupController.updateStudyGroup(1L, request);

        verify(notificationService, times(1))
                .notifyGroupCancelled(group);
    }

    // ── Test 7 : DELETE supprimer un groupe ───────────────────
    @Test
    void shouldDeleteStudyGroup() {
        doNothing().when(studyGroupService).deleteStudyGroup(1L);

        studyGroupController.deleteStudyGroup(1L);

        verify(studyGroupService, times(1)).deleteStudyGroup(1L);
    }

    // ── Test 8 : GET stats ────────────────────────────────────
    @Test
    void shouldGetStats() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalGroups", 5L);
        stats.put("activeGroups", 3L);
        stats.put("totalStudents", 20L);

        when(studyGroupService.getStats()).thenReturn(stats);

        ResponseEntity<Map<String, Object>> result =
                studyGroupController.getStats();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(5L, result.getBody().get("totalGroups"));
    }

    // ── Test 9 : GET recherche ────────────────────────────────
    @Test
    void shouldSearchGroups() {
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setName("English Beginners");
        dto.setLevel("BEGINNER");

        when(studyGroupService.searchGroups(
                eq("English"), any(), any(), any(), any()))
                .thenReturn(List.of(dto));

        ResponseEntity<List<StudyGroupResponseDTO>> result =
                studyGroupController.search(
                        "English", null, null, null, null);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals("English Beginners",
                result.getBody().get(0).getName());
    }

    // ── Test 10 : POST chatbot ────────────────────────────────
    @Test
    void shouldGetChatbotReply() {
        when(studyGroupService.chat("bonjour", null))
                .thenReturn("Bonjour ! Comment puis-je vous aider ?");

        ResponseEntity<Map<String, String>> result =
                studyGroupController.chat("bonjour", null);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("reply"));
    }

    // ── Test 11 : GET audit log ───────────────────────────────
    @Test
    void shouldGetAuditLog() {
        Map<String, Object> log = new LinkedHashMap<>();
        log.put("revision", 1);
        log.put("type", "INSERT");
        log.put("name", "Groupe A");

        when(studyGroupService.getAuditLog(1L))
                .thenReturn(List.of(log));

        ResponseEntity<List<Map<String, Object>>> result =
                studyGroupController.getAuditLog(1L);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertEquals("INSERT",
                result.getBody().get(0).get("type"));
    }

    // ── Test 12 : POST scheduler ──────────────────────────────
    @Test
    void shouldRunScheduler() {
        doNothing().when(schedulerService).updateStatuses();

        ResponseEntity<Map<String, Object>> result =
                studyGroupController.runScheduler();

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody()
                .containsKey("message"));
        verify(schedulerService, times(1)).updateStatuses();
    }
}