package tn.spring.course.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.mappers.Coursemappers;
import tn.spring.course.Models.Course;
import tn.spring.course.Repositories.CourseRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final Coursemappers courseMapper;

    @Override
    public CourseResponseDTO createCourse(CourseRequestDTO dto) {

        Course course = courseMapper.toEntity(dto);
        return courseMapper.toResponseDTO(courseRepository.save(course));
    }

    @Override
    public CourseResponseDTO updateCourse(int id, CourseRequestDTO dto) {

        Course existing = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));

        // update simple fields seulement
        existing.setTitle(dto.getTitle());
        existing.setDescription(dto.getDescription());
        existing.setDuration(dto.getDuration());
        existing.setAdminId(dto.getAdminId());

        return courseMapper.toResponseDTO(courseRepository.save(existing));
    }

    @Override
    public CourseResponseDTO getCourse(int id) {
        return courseMapper.toResponseDTO(
                courseRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Course not found"))
        );
    }

    @Override
    public List<CourseResponseDTO> getAllCourses() {
        return courseRepository.findAll()
                .stream()
                .map(courseMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCourse(int id) {
        courseRepository.deleteById(id);
    }
}