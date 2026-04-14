package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Services.CourseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CourseResponseDTO createCourse(@RequestBody CourseRequestDTO dto) {
        return courseService.createCourse(dto);
    }

    @GetMapping("/{id}")
    public CourseResponseDTO getCourse(@PathVariable int id) {
        return courseService.getCourse(id);
    }

    @GetMapping
    public List<CourseResponseDTO> getAllCourses() {
        return courseService.getAllCourses();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public CourseResponseDTO updateCourse(@PathVariable int id,
                                          @RequestBody CourseRequestDTO dto) {
        return courseService.updateCourse(id, dto);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public void deleteCourse(@PathVariable int id) {
        courseService.deleteCourse(id);
    }
    @GetMapping("/sort/duration")
    public List<CourseResponseDTO> sortByDuration(){
        return courseService.getCoursesSortedByDuration();
    }
    @GetMapping("/search")
    public List<CourseResponseDTO> searchCourses(@RequestParam String keyword){
        return courseService.searchCourses(keyword);
    }
    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf(){

        byte[] pdf = courseService.exportCoursesPdf();

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=courses.pdf")
                .header("Content-Type","application/pdf")
                .body(pdf);
    }
    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel(){

        byte[] excel = courseService.exportCoursesExcel();

        return ResponseEntity.ok()
                .header("Content-Disposition","attachment; filename=courses.xlsx")
                .header("Content-Type","application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .body(excel);
    }

}