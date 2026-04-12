package tn.spring.course.DTO;

import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data
public class CourseResponseDTO {

    private Integer courseid;
    private String title;
    private String description;
    private Integer duration;
    private UUID adminId;
    private List<Integer> contentIds;
    private List<Long> studyGroupIds;
}