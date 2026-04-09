package tn.spring.user.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateReportCommentRequest {

    @NotBlank(message = "COMMENT_MESSAGE_REQUIRED")
    @Size(max = 4000, message = "COMMENT_MESSAGE_TOO_LONG")
    private String message;
}
