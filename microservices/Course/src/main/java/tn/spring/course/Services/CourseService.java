package tn.spring.course.Services;

import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import java.util.List;

public interface CourseService {

    CourseResponseDTO createCourse(CourseRequestDTO dto);

    CourseResponseDTO updateCourse(int id, CourseRequestDTO dto);

    CourseResponseDTO getCourse(int id);

    List<CourseResponseDTO> getAllCourses();

    void deleteCourse(int id);
}