package tn.spring.quiz.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDetailsResponse {
    private Long id;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
    private Long quizId;
    private String quizTitle;
    private Integer score;
    private Boolean passed;
    private LocalDateTime submittedAt;
    private List<QuizAttemptAnswerResponse> answers;
}
