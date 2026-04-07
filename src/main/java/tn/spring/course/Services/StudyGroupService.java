package tn.spring.course.Services;

import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Models.StudyGroup;
import tn.spring.course.Models.StudyGroupStatus;
import java.util.Date;
import java.util.List;
import java.util.Map;

public interface StudyGroupService {

    StudyGroupResponseDTO createStudyGroup(StudyGroupRequestDTO dto);

    StudyGroupResponseDTO updateStudyGroup(Long id, StudyGroupRequestDTO dto);

    StudyGroupResponseDTO getStudyGroup(Long id);

    List<StudyGroupResponseDTO> getAllStudyGroups();

    void deleteStudyGroup(Long id);
    List<StudyGroupResponseDTO> getGroupsByDate(Date clickedDate);
    List<StudyGroupResponseDTO> getGroupsByMonth(int year, int month);
    Map<String, List<String>> getMarkedDates(int year, int month);
        Map<String, Object> getStats();
    // Pas de changement — reste String côté interface
    List<StudyGroupResponseDTO> searchGroups(
            String name,
            String level,
            String status,      // String ici c'est ok
            String location,
            Integer courseId
    );

    List<Map<String, Object>> getAuditLog(Long groupId);
    String chat(String message, Long groupId);
    StudyGroupResponseDTO createStudyGroupWithValidation(StudyGroupRequestDTO dto);

}