package tn.spring.course.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Mappers.StudyGroupMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudyGroupServiceTest {

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private StudyGroupMapper mapper;

    @InjectMocks
    private StudyGroupServiceImpl studyGroupService;

    // ── Test 1 : Récupérer tous les groupes ───────────────────
    @Test
    void shouldReturnAllStudyGroups() {
        StudyGroup group = new StudyGroup();
        group.setName("Groupe A");
        group.setLevel("BEGINNER");

        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setName("Groupe A");

        when(studyGroupRepository.findAllWithStudents())
                .thenReturn(List.of(group));
        when(mapper.toDTO(group)).thenReturn(dto);

        List<StudyGroupResponseDTO> result =
                studyGroupService.getAllStudyGroups();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Groupe A", result.get(0).getName());
        verify(studyGroupRepository, times(1)).findAllWithStudents();
    }

    // ── Test 2 : Récupérer un groupe par ID ───────────────────
    @Test
    void shouldReturnStudyGroupById() {
        StudyGroup group = new StudyGroup();
        group.setGroupId(1L);
        group.setName("Groupe B");

        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();
        dto.setGroupId(1L);
        dto.setName("Groupe B");

        when(studyGroupRepository.findById(1L))
                .thenReturn(Optional.of(group));
        when(mapper.toDTO(group)).thenReturn(dto);

        StudyGroupResponseDTO result = studyGroupService.getStudyGroup(1L);

        assertNotNull(result);
        assertEquals("Groupe B", result.getName());
        verify(studyGroupRepository, times(1)).findById(1L);
    }

    // ── Test 3 : Groupe introuvable ───────────────────────────
    @Test
    void shouldThrowExceptionWhenGroupNotFound() {
        when(studyGroupRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
                studyGroupService.getStudyGroup(99L)
        );
        verify(studyGroupRepository, times(1)).findById(99L);
    }

    // ── Test 4 : Créer un groupe ──────────────────────────────
    @Test
    void shouldCreateStudyGroup() {
        StudyGroupRequestDTO requestDTO = new StudyGroupRequestDTO();
        requestDTO.setCourseId(1);
        requestDTO.setName("Nouveau Groupe");
        requestDTO.setLevel("INTERMEDIATE");
        requestDTO.setMaxCapacity(20);
        requestDTO.setStatus(StudyGroupStatus.PLANNED);

        Course course = new Course();
        course.setCourseid(1);

        StudyGroup group = new StudyGroup();
        group.setName("Nouveau Groupe");

        StudyGroupResponseDTO responseDTO = new StudyGroupResponseDTO();
        responseDTO.setName("Nouveau Groupe");

        when(courseRepository.findById(1))
                .thenReturn(Optional.of(course));
        when(mapper.toEntity(requestDTO, course)).thenReturn(group);
        when(studyGroupRepository.save(group)).thenReturn(group);
        when(mapper.toDTO(group)).thenReturn(responseDTO);

        StudyGroupResponseDTO result =
                studyGroupService.createStudyGroup(requestDTO);

        assertNotNull(result);
        assertEquals("Nouveau Groupe", result.getName());
        verify(studyGroupRepository, times(1)).save(group);
    }

    // ── Test 5 : Supprimer un groupe ──────────────────────────
    @Test
    void shouldDeleteStudyGroup() {
        doNothing().when(studyGroupRepository).deleteById(1L);

        studyGroupService.deleteStudyGroup(1L);

        verify(studyGroupRepository, times(1)).deleteById(1L);
    }

    // ── Test 6 : Recherche par nom ────────────────────────────
    @Test
    void shouldFilterGroupsByName() {
        StudyGroup group1 = new StudyGroup();
        group1.setName("English Beginners");
        group1.setLevel("BEGINNER");
        group1.setStatus(StudyGroupStatus.ACTIVE);
        group1.setLocation("Tunis");

        StudyGroup group2 = new StudyGroup();
        group2.setName("Advanced Group");
        group2.setLevel("ADVANCED");
        group2.setStatus(StudyGroupStatus.ACTIVE);
        group2.setLocation("Sousse");

        StudyGroupResponseDTO dto1 = new StudyGroupResponseDTO();
        dto1.setName("English Beginners");

        when(studyGroupRepository.findAllWithStudents())
                .thenReturn(List.of(group1, group2));
        when(mapper.toDTO(group1)).thenReturn(dto1);

        List<StudyGroupResponseDTO> result =
                studyGroupService.searchGroups(
                        "English", null, null, null, null);

        assertEquals(1, result.size());
        assertEquals("English Beginners", result.get(0).getName());
    }

    // ── Test 7 : Recherche sans résultat ─────────────────────
    @Test
    void shouldReturnEmptyWhenNoMatch() {
        StudyGroup group = new StudyGroup();
        group.setName("Advanced Group");
        group.setLevel("ADVANCED");
        group.setStatus(StudyGroupStatus.ACTIVE);
        group.setLocation("Sousse");

        when(studyGroupRepository.findAllWithStudents())
                .thenReturn(List.of(group));

        List<StudyGroupResponseDTO> result =
                studyGroupService.searchGroups(
                        "Inexistant", null, null, null, null);

        assertTrue(result.isEmpty());
    }

    // ── Test 8 : Stats globales ───────────────────────────────
    @Test
    void shouldReturnStats() {
        StudyGroup group = new StudyGroup();
        group.setMaxCapacity(10);
        group.setStatus(StudyGroupStatus.ACTIVE);
        group.setStudentsIds(
                List.of(UUID.randomUUID(), UUID.randomUUID()));

        when(studyGroupRepository.findAllWithStudents())
                .thenReturn(List.of(group));
        when(studyGroupRepository.countByStatus())
                .thenReturn(Collections.emptyList());
        when(studyGroupRepository.countByLevel())
                .thenReturn(Collections.emptyList());
        when(studyGroupRepository.avgFillRateByLevel())
                .thenReturn(Collections.emptyList());
        when(studyGroupRepository.capacityVsEnrolledByLevel())
                .thenReturn(Collections.emptyList());
        when(studyGroupRepository.countByMonth())
                .thenReturn(Collections.emptyList());
        when(studyGroupRepository.findTopByFillRate(any()))
                .thenReturn(Collections.emptyList());

        Map<String, Object> stats = studyGroupService.getStats();

        assertNotNull(stats);
        assertEquals(1L, stats.get("totalGroups"));
        assertEquals(1L, stats.get("activeGroups"));
        assertEquals(2L, stats.get("totalStudents"));
    }

    // ── Test 9 : Groupes par date ─────────────────────────────
    @Test
    void shouldReturnGroupsByDate() {
        Date date = new Date();
        StudyGroup group = new StudyGroup();
        group.setName("Groupe Date");
        group.setStartdate(date);
        group.setEnddate(date);

        when(studyGroupRepository.findByDate(any()))
                .thenReturn(List.of(group));

        List<StudyGroupResponseDTO> result =
                studyGroupService.getGroupsByDate(date);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ── Test 10 : Groupes par mois ────────────────────────────
    @Test
    void shouldReturnGroupsByMonth() {
        StudyGroup group = new StudyGroup();
        group.setName("Groupe Mois");
        group.setStartdate(new Date());
        group.setEnddate(new Date());

        when(studyGroupRepository.findByMonthRange(any(), any()))
                .thenReturn(List.of(group));

        List<StudyGroupResponseDTO> result =
                studyGroupService.getGroupsByMonth(2025, 3);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    // ── Test 11 : Audit log ───────────────────────────────────
    @Test
    void shouldReturnAuditLog() {
        when(studyGroupRepository.findRevisions(1L))
                .thenReturn(org.springframework.data.history.Revisions.none());

        List<Map<String, Object>> log =
                studyGroupService.getAuditLog(1L);

        assertNotNull(log);
        assertTrue(log.isEmpty());
    }
    // ── Test 12 : Chat Gemini retourne un message ─────────────
    @Test
    void shouldReturnMessageFromChat() {
        String result = studyGroupService.chat("bonjour", null);

        assertNotNull(result);
        assertFalse(result.isEmpty());
    }
}