package tn.spring.quiz.DTO;


import lombok.Data;

import java.util.Map;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;

@Data
public class QuizSubmissionRequest {

    @NotNull(message = "L'identifiant du quiz est obligatoire")
    private Long quizId;

    @NotNull(message = "L'identifiant de l'étudiant est obligatoire")
    private UUID studentId;

    // questionId -> answerId choisi
    private Map<Long, Long> answers;
}