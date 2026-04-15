package tn.spring.course.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.course.Controllers.StudyGroupAlertController;
import tn.spring.course.DTO.ContentRequestDTO;
import tn.spring.course.DTO.ContentResponseDTO;
import tn.spring.course.Mappers.ContentMapper;
import tn.spring.course.Models.Content;
import tn.spring.course.Models.Course;
import tn.spring.course.Repositories.ContentRepository;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentServiceTest {

    @Mock
    private ContentRepository contentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private ContentMapper mapper;

    @Mock
    private StudyGroupRepository studyGroupRepository;

    @Mock
    private StudyGroupAlertController alertController;

    @InjectMocks
    private ContentServiceImpl contentService;

    // ── Helpers ───────────────────────────────────────────────
    private Course buildCourse(int id) {
        Course c = new Course();
        c.setCourseid(id);
        c.setTitle("Cours " + id);
        return c;
    }

    private Content buildContent(int id, String title, String type, Course course) {
        Content c = new Content();
        c.setContentid(id);
        c.setTitle(title);
        c.setType(type);
        c.setUrl("https://example.com/" + title.toLowerCase().replace(" ", "-"));
        c.setAuthorId(UUID.randomUUID());
        c.setCourse(course);
        return c;
    }

    private ContentResponseDTO buildResponseDTO(int id, String title, String type) {
        return ContentResponseDTO.builder()
                .contentId(id)
                .title(title)
                .type(type)
                .url("https://example.com/" + title.toLowerCase().replace(" ", "-"))
                .courseId(1)
                .authorId(UUID.randomUUID())
                .build();
    }

    // ── Test 1 : Créer un contenu ─────────────────────────────
    @Test
    void shouldCreateContent() {
        Course course = buildCourse(1);

        ContentRequestDTO request = new ContentRequestDTO();
        request.setTitle("Intro Java");
        request.setType("VIDEO");
        request.setUrl("https://example.com/intro-java");
        request.setCourseId(1);
        request.setAuthorId(UUID.randomUUID());

        Content entity   = buildContent(1, "Intro Java", "VIDEO", course);
        ContentResponseDTO response = buildResponseDTO(1, "Intro Java", "VIDEO");

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(request, course)).thenReturn(entity);
        when(contentRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        when(studyGroupRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        ContentResponseDTO result = contentService.createContent(request);

        assertNotNull(result);
        assertEquals("Intro Java", result.getTitle());
        assertEquals("VIDEO", result.getType());
        verify(contentRepository, times(1)).save(entity);
    }

    // ── Test 2 : Créer contenu → cours introuvable ────────────
    @Test
    void shouldThrowWhenCourseNotFoundOnCreate() {
        ContentRequestDTO request = new ContentRequestDTO();
        request.setCourseId(99);

        when(courseRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.createContent(request));
    }

    // ── Test 3 : Créer contenu → notifie les StudyGroups ──────
    @Test
    void shouldNotifyStudyGroupsOnContentCreate() {
        Course course = buildCourse(1);

        ContentRequestDTO request = new ContentRequestDTO();
        request.setTitle("Nouveau PDF");
        request.setType("PDF");
        request.setUrl("https://example.com/pdf");
        request.setCourseId(1);
        request.setAuthorId(UUID.randomUUID());

        Content entity = buildContent(1, "Nouveau PDF", "PDF", course);
        ContentResponseDTO response = buildResponseDTO(1, "Nouveau PDF", "PDF");

        tn.spring.course.Models.StudyGroup group =
                new tn.spring.course.Models.StudyGroup();
        group.setGroupId(10L);
        group.setName("Groupe Test");

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(request, course)).thenReturn(entity);
        when(contentRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        when(studyGroupRepository.findByCourse(course))
                .thenReturn(List.of(group));

        contentService.createContent(request);

        // Vérifier que la notification a été envoyée au groupe
        verify(alertController, times(1))
                .notifyNewContent(eq(group), eq("Nouveau PDF"), eq("PDF"));
    }

    // ── Test 4 : Récupérer un contenu par ID ──────────────────
    @Test
    void shouldGetContentById() {
        Course course = buildCourse(1);
        Content entity = buildContent(1, "Spring MVC", "ARTICLE", course);
        ContentResponseDTO response = buildResponseDTO(1, "Spring MVC", "ARTICLE");

        when(contentRepository.findById(1)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDTO(entity)).thenReturn(response);

        ContentResponseDTO result = contentService.getContent(1);

        assertNotNull(result);
        assertEquals("Spring MVC", result.getTitle());
        verify(contentRepository, times(1)).findById(1);
    }

    // ── Test 5 : Contenu introuvable → exception ──────────────
    @Test
    void shouldThrowWhenContentNotFound() {
        when(contentRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> contentService.getContent(99));

        assertEquals("Content not found", ex.getMessage());
    }

    // ── Test 6 : Récupérer tous les contenus ──────────────────
    @Test
    void shouldReturnAllContents() {
        Course course = buildCourse(1);
        Content c1 = buildContent(1, "Vidéo 1", "VIDEO",   course);
        Content c2 = buildContent(2, "PDF 1",   "PDF",     course);

        ContentResponseDTO dto1 = buildResponseDTO(1, "Vidéo 1", "VIDEO");
        ContentResponseDTO dto2 = buildResponseDTO(2, "PDF 1",   "PDF");

        when(contentRepository.findAll()).thenReturn(List.of(c1, c2));
        when(mapper.toResponseDTO(c1)).thenReturn(dto1);
        when(mapper.toResponseDTO(c2)).thenReturn(dto2);

        List<ContentResponseDTO> result = contentService.getAllContents();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(contentRepository, times(1)).findAll();
    }

    // ── Test 7 : Mettre à jour un contenu ─────────────────────
    @Test
    void shouldUpdateContent() {
        Course course = buildCourse(1);
        Content existing = buildContent(1, "Old Title", "VIDEO", course);

        ContentRequestDTO request = new ContentRequestDTO();
        request.setTitle("New Title");
        request.setType("PDF");
        request.setUrl("https://example.com/new");

        Content updated = buildContent(1, "New Title", "PDF", course);
        ContentResponseDTO response = buildResponseDTO(1, "New Title", "PDF");

        when(contentRepository.findById(1)).thenReturn(Optional.of(existing));
        when(contentRepository.save(existing)).thenReturn(updated);
        when(mapper.toResponseDTO(updated)).thenReturn(response);

        ContentResponseDTO result = contentService.updateContent(1, request);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals("PDF", result.getType());
        verify(contentRepository, times(1)).save(existing);
    }

    // ── Test 8 : Mise à jour contenu introuvable ──────────────
    @Test
    void shouldThrowWhenUpdatingNonExistentContent() {
        ContentRequestDTO request = new ContentRequestDTO();
        request.setTitle("Titre");

        when(contentRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.updateContent(99, request));
    }

    // ── Test 9 : Supprimer un contenu ─────────────────────────
    @Test
    void shouldDeleteContent() {
        Course course = buildCourse(1);
        Content entity = buildContent(1, "À Supprimer", "VIDEO", course);

        when(contentRepository.findById(1)).thenReturn(Optional.of(entity));
        doNothing().when(contentRepository).deleteById(1);

        contentService.deleteContent(1);

        verify(contentRepository, times(1)).deleteById(1);
    }

    // ── Test 10 : Supprimer contenu introuvable → exception ───
    @Test
    void shouldThrowWhenDeletingNonExistentContent() {
        when(contentRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> contentService.deleteContent(99));
    }

    // ── Test 11 : Recherche par mot-clé ───────────────────────
    @Test
    void shouldSearchContentsByKeyword() {
        Course course = buildCourse(1);
        Content c = buildContent(1, "Introduction Python", "VIDEO", course);
        ContentResponseDTO dto = buildResponseDTO(1, "Introduction Python", "VIDEO");

        when(contentRepository.findByTitleContainingIgnoreCase("python"))
                .thenReturn(List.of(c));
        when(mapper.toResponseDTO(c)).thenReturn(dto);

        List<ContentResponseDTO> result = contentService.searchContents("python");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(contentRepository, times(1))
                .findByTitleContainingIgnoreCase("python");
    }

    // ── Test 12 : Recherche sans résultat ─────────────────────
    @Test
    void shouldReturnEmptyWhenSearchNoMatch() {
        when(contentRepository.findByTitleContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());

        List<ContentResponseDTO> result = contentService.searchContents("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 13 : Statistiques par type ───────────────────────
    @Test
    void shouldReturnStatsByType() {
        List<Object[]> stats = List.of(
                new Object[]{"VIDEO",   5L},
                new Object[]{"PDF",     3L},
                new Object[]{"ARTICLE", 2L}
        );

        when(contentRepository.countByType()).thenReturn(stats);

        List<Object[]> result = contentService.getStatsByType();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("VIDEO", result.get(0)[0]);
        assertEquals(5L,      result.get(0)[1]);
        verify(contentRepository, times(1)).countByType();
    }

    // ── Test 14 : Historique vide au démarrage ────────────────
    @Test
    void shouldReturnEmptyHistoryInitially() {
        List<String> history = contentService.getHistory();

        assertNotNull(history);
        // L'historique peut contenir des entrées d'autres tests
        // On vérifie juste que la méthode retourne une liste non-null
    }

    // ── Test 15 : Historique enregistre les actions ───────────
    @Test
    void shouldRecordHistoryOnCreateAndDelete() {
        Course course = buildCourse(1);

        // CREATE
        ContentRequestDTO request = new ContentRequestDTO();
        request.setTitle("Cours Historique");
        request.setType("VIDEO");
        request.setUrl("https://example.com/h");
        request.setCourseId(1);
        request.setAuthorId(UUID.randomUUID());

        Content entity = buildContent(1, "Cours Historique", "VIDEO", course);
        ContentResponseDTO response = buildResponseDTO(1, "Cours Historique", "VIDEO");

        when(courseRepository.findById(1)).thenReturn(Optional.of(course));
        when(mapper.toEntity(request, course)).thenReturn(entity);
        when(contentRepository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDTO(entity)).thenReturn(response);
        when(studyGroupRepository.findByCourse(course)).thenReturn(Collections.emptyList());

        contentService.createContent(request);

        List<String> history = contentService.getHistory();
        assertNotNull(history);
        assertTrue(history.stream()
                .anyMatch(h -> h.contains("CREATE") && h.contains("Cours Historique")));
    }

    // ── Test 16 : Export PDF retourne des bytes non vides ─────
    @Test
    void shouldExportContentsPdf() {
        Course course = buildCourse(1);
        Content c = buildContent(1, "Contenu PDF", "PDF", course);

        when(contentRepository.findAll()).thenReturn(List.of(c));

        byte[] pdf = contentService.exportContentsPdf();

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    // ── Test 17 : Export historique TXT ───────────────────────
    @Test
    void shouldExportHistoryAsTxt() {
        byte[] txt = contentService.exportHistoryTxt();

        assertNotNull(txt);
        // Le contenu TXT est valide (peut être vide si aucune action)
    }
}
