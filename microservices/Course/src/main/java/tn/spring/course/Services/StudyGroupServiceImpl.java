package tn.spring.course.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.mappers.StudyGroupMapper;
import tn.spring.course.Models.Course;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Repositories.CourseRepository;
import tn.spring.course.Repositories.StudyGroupRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyGroupServiceImpl implements StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final CourseRepository courseRepository;
    private final StudyGroupMapper mapper;

    @Override
    public StudyGroupResponseDTO createStudyGroup(StudyGroupRequestDTO dto) {

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        StudyGroup group = mapper.toEntity(dto, course);

        return mapper.toDTO(studyGroupRepository.save(group));
    }

    @Override
    public StudyGroupResponseDTO updateStudyGroup(Long id, StudyGroupRequestDTO dto) {

        StudyGroup existing = studyGroupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("StudyGroup not found"));

        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        existing.setName(dto.getName());
        existing.setLevel(dto.getLevel());
        existing.setLocation(dto.getLocation());
        existing.setMaxCapacity(dto.getMaxCapacity());
        existing.setStartdate(dto.getStartdate());
        existing.setEnddate(dto.getEnddate());
        existing.setCourse(course);
        existing.setTutorId(dto.getTutorId());
        existing.setStudentsIds(dto.getStudentsIds());

        return mapper.toDTO(studyGroupRepository.save(existing));
    }

    @Override
    public StudyGroupResponseDTO getStudyGroup(Long id) {
        return mapper.toDTO(
                studyGroupRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("StudyGroup not found"))
        );
    }

    @Override
    public List<StudyGroupResponseDTO> getAllStudyGroups() {
        return studyGroupRepository.findAll()
                .stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStudyGroup(Long id) {
        studyGroupRepository.deleteById(id);
    }
}