package tn.spring.quiz.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long questionId;
    private String questionText;

    private Long selectedAnswerId;
    private String selectedAnswerText;

    private Long correctAnswerId;
    private String correctAnswerText;

    private Boolean answered;
    private Boolean correct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private QuizAttempt quizAttempt;
}
