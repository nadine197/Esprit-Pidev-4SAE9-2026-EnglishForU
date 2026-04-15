package tn.spring.course.DTO;
import tn.spring.course.Models.StudyGroupStatus;
import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class StudyGroupRequestDTO {

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