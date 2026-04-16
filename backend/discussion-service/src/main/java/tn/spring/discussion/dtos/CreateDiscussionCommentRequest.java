package tn.spring.discussion.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDiscussionCommentRequest {

    @NotBlank(message = "COMMENT_MESSAGE_REQUIRED")
    @Size(max = 2000, message = "COMMENT_MESSAGE_TOO_LONG")
    private String message;
}
