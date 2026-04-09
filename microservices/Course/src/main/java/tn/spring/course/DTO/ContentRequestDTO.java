package tn.spring.course.DTO;
import lombok.Data;
@Data
public class ContentRequestDTO {

    private String title;
    private String type;
    private String url;
    private Integer courseId;
}
