package tn.spring.course.Services;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Mappers.CourseMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.Repositories.CourseRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CourseMapper courseMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    // ── Helper : construire un Course entity ──────────────────
    private Course buildCourse(int id, String title, int duration) {
        Course c = new Course();
        c.setCourseid(id);
        c.setTitle(title);
        c.setDescription("Description de " + title);
        c.setDuration(duration);
        c.setAdminId(UUID.randomUUID());
        return c;
    }

    // ── Helper : construire un CourseResponseDTO ──────────────
    private CourseResponseDTO buildResponseDTO(int id, String title, int duration) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setCourseid(id);
        dto.setTitle(title);
        dto.setDescription("Description de " + title);
        dto.setDuration(duration);
        return dto;
    }

    // ── Test 1 : Créer un cours ───────────────────────────────
    @Test
    void shouldCreateCourse() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Java Avancé");
        request.setDescription("Cours Java niveau avancé");
        request.setDuration(40);
        request.setAdminId(UUID.randomUUID());

        Course entity   = buildCourse(1, "Java Avancé", 40);
        CourseResponseDTO response = buildResponseDTO(1, "Java Avancé", 40);

        when(courseMapper.toEntity(request)).thenReturn(entity);
        when(courseRepository.save(entity)).thenReturn(entity);
        when(courseMapper.toResponseDTO(entity)).thenReturn(response);

        CourseResponseDTO result = courseService.createCourse(request);

        assertNotNull(result);
        assertEquals("Java Avancé", result.getTitle());
        assertEquals(40, result.getDuration());
        verify(courseRepository, times(1)).save(entity);
    }

    // ── Test 2 : Récupérer un cours par ID ────────────────────
    @Test
    void shouldGetCourseById() {
        Course entity = buildCourse(1, "Spring Boot", 30);
        CourseResponseDTO response = buildResponseDTO(1, "Spring Boot", 30);

        when(courseRepository.findById(1)).thenReturn(Optional.of(entity));
        when(courseMapper.toResponseDTO(entity)).thenReturn(response);

        CourseResponseDTO result = courseService.getCourse(1);

        assertNotNull(result);
        assertEquals(1, result.getCourseid());
        assertEquals("Spring Boot", result.getTitle());
        verify(courseRepository, times(1)).findById(1);
    }

    // ── Test 3 : Cours introuvable → exception ────────────────
    @Test
    void shouldThrowExceptionWhenCourseNotFound() {
        when(courseRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseService.getCourse(99));

        assertEquals("Course not found", ex.getMessage());
        verify(courseRepository, times(1)).findById(99);
    }

    // ── Test 4 : Récupérer tous les cours ─────────────────────
    @Test
    void shouldReturnAllCourses() {
        Course c1 = buildCourse(1, "Angular", 20);
        Course c2 = buildCourse(2, "React",   25);

        CourseResponseDTO dto1 = buildResponseDTO(1, "Angular", 20);
        CourseResponseDTO dto2 = buildResponseDTO(2, "React",   25);

        when(courseRepository.findAll()).thenReturn(List.of(c1, c2));
        when(courseMapper.toResponseDTO(c1)).thenReturn(dto1);
        when(courseMapper.toResponseDTO(c2)).thenReturn(dto2);

        List<CourseResponseDTO> result = courseService.getAllCourses();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }

    // ── Test 5 : Mettre à jour un cours ───────────────────────
    @Test
    void shouldUpdateCourse() {
        Course existing = buildCourse(1, "Old Title", 10);

        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("New Title");
        request.setDescription("Nouvelle description");
        request.setDuration(50);
        request.setAdminId(existing.getAdminId());

        Course updated = buildCourse(1, "New Title", 50);
        CourseResponseDTO response = buildResponseDTO(1, "New Title", 50);

        when(courseRepository.findById(1)).thenReturn(Optional.of(existing));
        when(courseRepository.save(existing)).thenReturn(updated);
        when(courseMapper.toResponseDTO(updated)).thenReturn(response);

        CourseResponseDTO result = courseService.updateCourse(1, request);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        assertEquals(50, result.getDuration());
        verify(courseRepository, times(1)).save(existing);
    }

    // ── Test 6 : Mise à jour cours introuvable → exception ────
    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentCourse() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Titre");

        when(courseRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> courseService.updateCourse(99, request));
    }

    // ── Test 7 : Supprimer un cours ───────────────────────────
    @Test
    void shouldDeleteCourse() {
        doNothing().when(courseRepository).deleteById(1);

        courseService.deleteCourse(1);

        verify(courseRepository, times(1)).deleteById(1);
    }

    // ── Test 8 : Tri par durée croissante ─────────────────────
    @Test
    void shouldReturnCoursesSortedByDuration() {
        Course c1 = buildCourse(1, "Cours Court",  10);
        Course c2 = buildCourse(2, "Cours Moyen",  30);
        Course c3 = buildCourse(3, "Cours Long",   60);

        CourseResponseDTO dto1 = buildResponseDTO(1, "Cours Court",  10);
        CourseResponseDTO dto2 = buildResponseDTO(2, "Cours Moyen",  30);
        CourseResponseDTO dto3 = buildResponseDTO(3, "Cours Long",   60);

        when(courseRepository.findAllByOrderByDurationAsc())
                .thenReturn(List.of(c1, c2, c3));
        when(courseMapper.toResponseDTO(c1)).thenReturn(dto1);
        when(courseMapper.toResponseDTO(c2)).thenReturn(dto2);
        when(courseMapper.toResponseDTO(c3)).thenReturn(dto3);

        List<CourseResponseDTO> result = courseService.getCoursesSortedByDuration();

        assertNotNull(result);
        assertEquals(3, result.size());
        // Vérifier l'ordre croissant
        assertTrue(result.get(0).getDuration() <= result.get(1).getDuration());
        assertTrue(result.get(1).getDuration() <= result.get(2).getDuration());
        verify(courseRepository, times(1)).findAllByOrderByDurationAsc();
    }

    // ── Test 9 : Recherche par mot-clé (résultat trouvé) ──────
    @Test
    void shouldSearchCoursesByKeyword() {
        Course c = buildCourse(1, "Java Spring", 30);
        CourseResponseDTO dto = buildResponseDTO(1, "Java Spring", 30);

        when(courseRepository.findByTitleContainingIgnoreCase("java"))
                .thenReturn(List.of(c));
        when(courseMapper.toResponseDTO(c)).thenReturn(dto);

        List<CourseResponseDTO> result = courseService.searchCourses("java");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().toLowerCase().contains("java"));
        verify(courseRepository, times(1))
                .findByTitleContainingIgnoreCase("java");
    }

    // ── Test 10 : Recherche sans résultat ─────────────────────
    @Test
    void shouldReturnEmptyListWhenNoSearchMatch() {
        when(courseRepository.findByTitleContainingIgnoreCase("xyz"))
                .thenReturn(Collections.emptyList());

        List<CourseResponseDTO> result = courseService.searchCourses("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 11 : Recherche insensible à la casse ─────────────
    @Test
    void shouldSearchCaseInsensitive() {
        Course c = buildCourse(1, "PYTHON AVANCÉ", 25);
        CourseResponseDTO dto = buildResponseDTO(1, "PYTHON AVANCÉ", 25);

        when(courseRepository.findByTitleContainingIgnoreCase("python"))
                .thenReturn(List.of(c));
        when(courseMapper.toResponseDTO(c)).thenReturn(dto);

        List<CourseResponseDTO> result = courseService.searchCourses("python");

        assertEquals(1, result.size());
    }

    // ── Test 12 : Export PDF retourne des bytes non vides ─────
    @Test
    void shouldExportCoursesPdfWithData() {
        Course c = buildCourse(1, "DevOps", 35);
        when(courseRepository.findAll()).thenReturn(List.of(c));

        byte[] pdf = courseService.exportCoursesPdf();

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    // ── Test 13 : Export PDF liste vide ───────────────────────
    @Test
    void shouldExportEmptyPdf() {
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] pdf = courseService.exportCoursesPdf();

        assertNotNull(pdf);
        assertTrue(pdf.length > 0); // PDF valide même vide
    }

    // ── Test 14 : Export Excel retourne des bytes non vides ───
    @Test
    void shouldExportCoursesExcelWithData() {
        Course c = buildCourse(1, "Microservices", 45);
        when(courseRepository.findAll()).thenReturn(List.of(c));

        byte[] excel = courseService.exportCoursesExcel();

        assertNotNull(excel);
        assertTrue(excel.length > 0);
    }

    // ── Test 15 : Export Excel liste vide ─────────────────────
    @Test
    void shouldExportEmptyExcel() {
        when(courseRepository.findAll()).thenReturn(Collections.emptyList());

        byte[] excel = courseService.exportCoursesExcel();

        assertNotNull(excel);
        assertTrue(excel.length > 0); // Fichier Excel valide même vide
    }
}
