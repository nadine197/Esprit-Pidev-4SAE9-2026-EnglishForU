package tn.spring.course.DTO;

import jakarta.persistence.Enumerated;
import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import tn.spring.course.Models.StudyGroupStatus;

@Data
public class StudyGroupResponseDTO {

    private Long groupId;
    private String name;
    private String level;
    private String location;
    private int maxCapacity;
    private Date startdate;
    private Date enddate;
    private StudyGroupStatus status;
    private Integer courseId;
    private UUID tutorId;
    private List<UUID> studentsIds;
}