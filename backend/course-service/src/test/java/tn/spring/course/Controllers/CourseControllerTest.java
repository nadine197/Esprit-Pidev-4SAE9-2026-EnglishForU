package tn.spring.course.Controllers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Services.CourseService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    @InjectMocks
    private CourseController courseController;

    // ── Helper ────────────────────────────────────────────────
    private CourseResponseDTO buildDTO(int id, String title, int duration) {
        CourseResponseDTO dto = new CourseResponseDTO();
        dto.setCourseid(id);
        dto.setTitle(title);
        dto.setDescription("Description " + title);
        dto.setDuration(duration);
        return dto;
    }

    // ── Test 1 : POST créer un cours ──────────────────────────
    @Test
    void shouldCreateCourse() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Docker & Kubernetes");
        request.setDescription("Conteneurisation");
        request.setDuration(30);
        request.setAdminId(UUID.randomUUID());

        CourseResponseDTO response = buildDTO(1, "Docker & Kubernetes", 30);

        when(courseService.createCourse(any())).thenReturn(response);

        CourseResponseDTO result = courseController.createCourse(request);

        assertNotNull(result);
        assertEquals("Docker & Kubernetes", result.getTitle());
        verify(courseService, times(1)).createCourse(any());
    }

    // ── Test 2 : GET cours par ID ─────────────────────────────
    @Test
    void shouldGetCourseById() {
        CourseResponseDTO response = buildDTO(1, "Spring Security", 20);

        when(courseService.getCourse(1)).thenReturn(response);

        CourseResponseDTO result = courseController.getCourse(1);

        assertNotNull(result);
        assertEquals(1, result.getCourseid());
        assertEquals("Spring Security", result.getTitle());
        verify(courseService, times(1)).getCourse(1);
    }

    // ── Test 3 : GET cours introuvable → exception propagée ───
    @Test
    void shouldThrowWhenCourseNotFound() {
        when(courseService.getCourse(99))
                .thenThrow(new RuntimeException("Course not found"));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> courseController.getCourse(99));

        assertEquals("Course not found", ex.getMessage());
    }

    // ── Test 4 : GET tous les cours ───────────────────────────
    @Test
    void shouldGetAllCourses() {
        List<CourseResponseDTO> list = List.of(
                buildDTO(1, "Java",   30),
                buildDTO(2, "Python", 25)
        );

        when(courseService.getAllCourses()).thenReturn(list);

        List<CourseResponseDTO> result = courseController.getAllCourses();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(courseService, times(1)).getAllCourses();
    }

    // ── Test 5 : GET liste vide ───────────────────────────────
    @Test
    void shouldReturnEmptyListWhenNoCourses() {
        when(courseService.getAllCourses()).thenReturn(Collections.emptyList());

        List<CourseResponseDTO> result = courseController.getAllCourses();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 6 : PUT mettre à jour un cours ───────────────────
    @Test
    void shouldUpdateCourse() {
        CourseRequestDTO request = new CourseRequestDTO();
        request.setTitle("Titre Modifié");
        request.setDescription("Nouvelle desc");
        request.setDuration(55);

        CourseResponseDTO response = buildDTO(1, "Titre Modifié", 55);

        when(courseService.updateCourse(eq(1), any())).thenReturn(response);

        CourseResponseDTO result = courseController.updateCourse(1, request);

        assertNotNull(result);
        assertEquals("Titre Modifié", result.getTitle());
        assertEquals(55, result.getDuration());
        verify(courseService, times(1)).updateCourse(eq(1), any());
    }

    // ── Test 7 : DELETE supprimer un cours ────────────────────
    @Test
    void shouldDeleteCourse() {
        doNothing().when(courseService).deleteCourse(1);

        courseController.deleteCourse(1);

        verify(courseService, times(1)).deleteCourse(1);
    }

    // ── Test 8 : GET tri par durée ────────────────────────────
    @Test
    void shouldReturnCoursesSortedByDuration() {
        List<CourseResponseDTO> sorted = List.of(
                buildDTO(1, "Cours Court",  10),
                buildDTO(2, "Cours Moyen",  30),
                buildDTO(3, "Cours Long",   60)
        );

        when(courseService.getCoursesSortedByDuration()).thenReturn(sorted);

        List<CourseResponseDTO> result = courseController.sortByDuration();

        assertNotNull(result);
        assertEquals(3, result.size());
        // Vérifier l'ordre croissant
        assertTrue(result.get(0).getDuration() <= result.get(1).getDuration());
        assertTrue(result.get(1).getDuration() <= result.get(2).getDuration());
        verify(courseService, times(1)).getCoursesSortedByDuration();
    }

    // ── Test 9 : GET recherche par mot-clé ────────────────────
    @Test
    void shouldSearchCoursesByKeyword() {
        List<CourseResponseDTO> found = List.of(
                buildDTO(1, "Java Spring", 30)
        );

        when(courseService.searchCourses("spring")).thenReturn(found);

        List<CourseResponseDTO> result = courseController.searchCourses("spring");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(courseService, times(1)).searchCourses("spring");
    }

    // ── Test 10 : GET recherche sans résultat ─────────────────
    @Test
    void shouldReturnEmptyWhenSearchNoMatch() {
        when(courseService.searchCourses("xyz"))
                .thenReturn(Collections.emptyList());

        List<CourseResponseDTO> result = courseController.searchCourses("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ── Test 11 : GET export PDF ──────────────────────────────
    @Test
    void shouldExportPdfWithCorrectHeaders() {
        byte[] fakePdf = "PDF_CONTENT".getBytes();

        when(courseService.exportCoursesPdf()).thenReturn(fakePdf);

        ResponseEntity<byte[]> response = courseController.exportPdf();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertEquals("application/pdf",
                response.getHeaders().getFirst("Content-Type"));
        assertTrue(Objects.requireNonNull(
                response.getHeaders().getFirst("Content-Disposition"))
                .contains("courses.pdf"));
        verify(courseService, times(1)).exportCoursesPdf();
    }

    // ── Test 12 : GET export Excel ────────────────────────────
    @Test
    void shouldExportExcelWithCorrectHeaders() {
        byte[] fakeExcel = "EXCEL_CONTENT".getBytes();

        when(courseService.exportCoursesExcel()).thenReturn(fakeExcel);

        ResponseEntity<byte[]> response = courseController.exportExcel();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length > 0);
        assertTrue(Objects.requireNonNull(
                response.getHeaders().getFirst("Content-Type"))
                .contains("spreadsheetml"));
        assertTrue(Objects.requireNonNull(
                response.getHeaders().getFirst("Content-Disposition"))
                .contains("courses.xlsx"));
        verify(courseService, times(1)).exportCoursesExcel();
    }
}
