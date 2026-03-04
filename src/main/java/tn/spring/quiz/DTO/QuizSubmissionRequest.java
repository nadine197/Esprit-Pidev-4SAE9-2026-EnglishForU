package tn.spring.quiz.DTO;


import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class QuizSubmissionRequest {

    private Long quizId;
    private UUID studentId;

    // questionId -> answerId choisi
    private Map<Long, Long> answers;
}