package tn.spring.quiz.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class QuizRecommendationAnswerDTO {
    private String text;
    private boolean correct;
}
