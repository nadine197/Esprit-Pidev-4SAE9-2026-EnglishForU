package tn.spring.course.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class CourseRequestDTO {
    private String title;
    private String description;
    private Integer duration;
    private UUID adminId;
}