package tn.spring.course.DTO;

import lombok.Data;

@Data
public class ContentResponseDTO {
    private Integer contentId;
    private String title;
    private String type;
    private String url;
    private Integer courseId;
}