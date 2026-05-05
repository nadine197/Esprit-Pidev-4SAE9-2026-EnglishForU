package tn.spring.quiz.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class QuizSubmissionRequest {

    @NotNull(message = "Quiz id is required")
    private Long quizId;

    @NotNull(message = "Student id is required")
    private UUID studentId;

    private String studentName;

    private String studentEmail;

    // questionId -> selected answerId
    private Map<Long, Long> answers;
}
