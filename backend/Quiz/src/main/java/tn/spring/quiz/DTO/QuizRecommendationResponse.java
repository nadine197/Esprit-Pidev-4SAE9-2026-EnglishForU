package tn.spring.quiz.DTO;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class QuizRecommendationResponse {
    private String courseTitle;
    private String recommendationReason;
    private String questionText;
    private List<QuizRecommendationAnswerDTO> answers = new ArrayList<>();
}
