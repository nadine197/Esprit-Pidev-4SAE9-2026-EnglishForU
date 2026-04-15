package tn.spring.quiz.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptAnswerResponse {
    private Long id;
    private Long questionId;
    private String questionText;
    private Long selectedAnswerId;
    private String selectedAnswerText;
    private Long correctAnswerId;
    private String correctAnswerText;
    private Boolean answered;
    private Boolean correct;
}
