package tn.spring.course.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class ContentRequestDTO {

    private String title;
    private String type;
    private String url;

    private Integer courseId;
    private UUID authorId;
}