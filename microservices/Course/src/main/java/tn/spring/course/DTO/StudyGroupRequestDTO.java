package tn.spring.course.DTO;

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

    private Integer courseId;
    private UUID tutorId;
    private List<UUID> studentsIds;
}