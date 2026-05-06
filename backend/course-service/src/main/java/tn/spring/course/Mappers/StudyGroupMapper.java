package tn.spring.course.Mappers;

import org.springframework.stereotype.Component;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Models.Course;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
@Component
public class StudyGroupMapper {

    public StudyGroup toEntity(StudyGroupRequestDTO dto, Course course){
        return StudyGroup.builder()
                .name(dto.getName())
                .level(dto.getLevel())
                .location(dto.getLocation())
                .maxCapacity(dto.getMaxCapacity())
                .startdate(dto.getStartdate())
                .enddate(dto.getEnddate())
                .status(dto.getStatus())
                .course(course)
                .tutorId(dto.getTutorId())
                .studentsIds(dto.getStudentsIds())
                .build();
    }

    public StudyGroupResponseDTO toDTO(StudyGroup group){
        StudyGroupResponseDTO dto = new StudyGroupResponseDTO();

        dto.setGroupId(group.getGroupId());
        dto.setName(group.getName());
        dto.setLevel(group.getLevel());
        dto.setLocation(group.getLocation());
        dto.setMaxCapacity(group.getMaxCapacity());
        dto.setStartdate(group.getStartdate());
        dto.setEnddate(group.getEnddate());
        dto.setStatus(group.getStatus());
        dto.setCourseId(group.getCourse() != null ? group.getCourse().getCourseid(): null);
        dto.setTutorId(group.getTutorId());
        dto.setStudentsIds(group.getStudentsIds());

        return dto;
    }
}