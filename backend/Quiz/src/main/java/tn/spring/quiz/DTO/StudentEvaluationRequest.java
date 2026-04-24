package tn.spring.quiz.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StudentEvaluationRequest {

    @NotNull(message = "Student id is required")
    private UUID studentId;

    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student email is required")
    private String studentEmail;

    private Long quizAttemptId;

    @NotBlank(message = "Evaluation title is required")
    private String title;

    @NotBlank(message = "Feedback is required")
    private String feedback;

    private String strengths;

    private String areasToImprove;

    private String recommendedActions;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
}
