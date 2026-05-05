package tn.spring.course.DTO;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class ContentResponseDTO {

    private Integer contentId;
    private String title;
    private String type;
    private String url;

    private Integer courseId;
    private UUID authorId;
}