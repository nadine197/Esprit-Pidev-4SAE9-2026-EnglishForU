package tn.spring.quiz.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.spring.quiz.DTO.QuizRecommendationResponse;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Services.GeminiQuizRecommendationService;
import tn.spring.quiz.Services.QuestionService;
import tn.spring.quiz.Services.QuizService;

import java.util.List;

import jakarta.validation.Valid;
@CrossOrigin(origins = {"http://localhost:4200", "*", "*"})
@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;
    private final QuestionService questionService;
    private final GeminiQuizRecommendationService geminiQuizRecommendationService;

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<Quiz> createQuiz(@PathVariable Long courseId, @Valid @RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.createQuiz(courseId, quiz));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @Valid @RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quiz));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }

    @PostMapping("/{quizId}/recommendations")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<QuizRecommendationResponse> recommendQuestion(@PathVariable Long quizId) {
        return ResponseEntity.ok(geminiQuizRecommendationService.recommendQuestion(quizId));
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<Question>> getQuizQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizQuestions(quizId));
    }

    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<List<Answer>> getAnswerByQuizQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(quizService.getAnswerByQuizQuestion(questionId));
    }

    @PostMapping("/quiz/{quizId}")
    public ResponseEntity<Question> createQuestion(@PathVariable Long quizId, @Valid @RequestBody Question question) {
        return ResponseEntity.ok(questionService.createQuestion(quizId, question));
    }

    @PutMapping("/question/{questionId}")
    public ResponseEntity<Question> updateQuestion(@PathVariable Long questionId, @Valid @RequestBody Question question) {
        return ResponseEntity.ok(questionService.updateQuestion(questionId, question));
    }

    @PostMapping("/quizAddQuestion/{quizId}")
    public ResponseEntity<Question> addQuestion(@PathVariable Long quizId, @Valid @RequestBody Question question) {
        return ResponseEntity.ok(questionService.addQuestion(quizId, question));
    }

    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{questionId}/answers")
    public ResponseEntity<Answer> addAnswer(@PathVariable Long questionId, @Valid @RequestBody Answer answer) {
        return ResponseEntity.ok(questionService.addAnswer(questionId, answer));
    }

    @PutMapping("/answers/{answerId}")
    public ResponseEntity<Answer> updateAnswer(@PathVariable Long answerId, @Valid @RequestBody Answer answerDetails) {
        return ResponseEntity.ok(questionService.updateAnswer(answerId, answerDetails));
    }

    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long answerId) {
        questionService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}
