package tn.spring.quiz.Feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.List;

@FeignClient(name = "Course")
public interface CourseClient {

    @GetMapping("/api/courses")
    List<CourseDTO> getAllCourses();

    @GetMapping("/api/courses/{id}")
    CourseDTO getCourseById(@PathVariable("id") int id);
}