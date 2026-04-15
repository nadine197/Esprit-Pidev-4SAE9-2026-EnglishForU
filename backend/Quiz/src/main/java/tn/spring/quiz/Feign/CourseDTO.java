package tn.spring.quiz.Feign;

import lombok.Data;

@Data
public class CourseDTO {
    private Integer courseid;
    private String title;
    private String description;
    private Integer duration;
    private java.util.UUID adminId;
}