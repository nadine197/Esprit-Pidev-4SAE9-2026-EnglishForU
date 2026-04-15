package tn.spring.quiz.DTO;

import lombok.Data;

@Data
public class EvaluationMotivationSuggestionResponse {

    private String studentName;

    private String headline;

    private String motivationMessage;

    private String strengthsSuggestion;

    private String recommendedActionsSuggestion;

    private String coachTip;
}
