package tn.spring.course.Mappers;

import org.springframework.stereotype.Component;
import tn.spring.course.DTO.CourseRequestDTO;
import tn.spring.course.DTO.CourseResponseDTO;
import tn.spring.course.Models.Course;

import java.util.stream.Collectors;

@Component
public class CourseMapper {

    // DTO -> ENTITY
    public Course toEntity(CourseRequestDTO dto){
        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setDescription(dto.getDescription());
        course.setDuration(dto.getDuration());
        course.setAdminId(dto.getAdminId());
        return course;
    }

    // ENTITY -> DTO
    public CourseResponseDTO toResponseDTO(Course course){
        CourseResponseDTO dto = new CourseResponseDTO();

        dto.setCourseid(course.getCourseid());
        dto.setTitle(course.getTitle());
        dto.setDescription(course.getDescription());
        dto.setDuration(course.getDuration());
        dto.setAdminId(course.getAdminId());

        if(course.getContents()!=null)
            dto.setContentIds(
                    course.getContents()
                            .stream()
                            .map(c -> c.getContentid())
                            .collect(Collectors.toList())
            );

        if(course.getStudyGroups()!=null)
            dto.setStudyGroupIds(
                    course.getStudyGroups()
                            .stream()
                            .map(g -> g.getGroupId())
                            .collect(Collectors.toList())
            );

        return dto;
    }
}