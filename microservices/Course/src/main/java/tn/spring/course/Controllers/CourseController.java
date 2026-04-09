package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Services.CourseService;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/addcourse")
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
    public CourseResponseDTO updateCourse(@PathVariable int id,
                                          @RequestBody CourseRequestDTO dto) {
        return courseService.updateCourse(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable int id) {
        courseService.deleteCourse(id);
    }
}