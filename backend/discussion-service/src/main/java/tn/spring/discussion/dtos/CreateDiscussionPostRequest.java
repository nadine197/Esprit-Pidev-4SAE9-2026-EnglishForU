package tn.spring.discussion.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.spring.discussion.enums.DiscussionPostType;

@Data
public class CreateDiscussionPostRequest {

    @NotBlank(message = "COURSE_ID_REQUIRED")
    private String courseId;

    @NotNull(message = "POST_TYPE_REQUIRED")
    private DiscussionPostType type;

    private String content;
    private String imagePath;
    private String quizPayload;

    private String targetRole;
    private String targetLevel;

    private String authorLevel;
}
