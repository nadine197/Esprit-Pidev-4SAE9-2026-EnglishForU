package tn.spring.quiz.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Repositories.CourseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

    private final CourseRepository courseRepository;

    // ✅ GET ALL COURSES
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    // ✅ GET COURSE BY ID
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }
}