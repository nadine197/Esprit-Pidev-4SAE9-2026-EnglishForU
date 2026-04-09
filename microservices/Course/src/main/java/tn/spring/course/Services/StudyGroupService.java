package tn.spring.course.Services;

import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;

import java.util.List;

public interface StudyGroupService {

    StudyGroupResponseDTO createStudyGroup(StudyGroupRequestDTO dto);

    StudyGroupResponseDTO updateStudyGroup(Long id, StudyGroupRequestDTO dto);

    StudyGroupResponseDTO getStudyGroup(Long id);

    List<StudyGroupResponseDTO> getAllStudyGroups();

    void deleteStudyGroup(Long id);
}