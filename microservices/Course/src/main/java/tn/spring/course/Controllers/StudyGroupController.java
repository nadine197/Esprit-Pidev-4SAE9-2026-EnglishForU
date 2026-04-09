package tn.spring.course.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.spring.course.DTO.StudyGroupRequestDTO;
import tn.spring.course.DTO.StudyGroupResponseDTO;
import tn.spring.course.Services.StudyGroupService;

import java.util.List;

@RestController
@RequestMapping("/api/study-groups")
@RequiredArgsConstructor
public class StudyGroupController {

    private final StudyGroupService studyGroupService;

    @PostMapping("/addgroup")
    public StudyGroupResponseDTO createStudyGroup(@RequestBody StudyGroupRequestDTO dto) {
        return studyGroupService.createStudyGroup(dto);
    }

    @GetMapping
    public List<StudyGroupResponseDTO> getAllStudyGroups() {
        return studyGroupService.getAllStudyGroups();
    }

    @GetMapping("/{id}")
    public StudyGroupResponseDTO getStudyGroup(@PathVariable Long id) {
        return studyGroupService.getStudyGroup(id);
    }

    @PutMapping("/{id}")
    public StudyGroupResponseDTO updateStudyGroup(@PathVariable Long id,
                                                  @RequestBody StudyGroupRequestDTO dto) {
        return studyGroupService.updateStudyGroup(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteStudyGroup(@PathVariable Long id) {
        studyGroupService.deleteStudyGroup(id);
    }
}