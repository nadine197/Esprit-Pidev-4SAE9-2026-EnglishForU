package tn.spring.quiz.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.spring.quiz.DTO.QuizDTO;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Services.QuestionService;
import tn.spring.quiz.Services.QuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizController {
    @Autowired
    private final QuizService quizService;
    private final QuestionService questionService;

    // ----------- CRUD Admin & Teacher ------------



    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<Quiz> createQuiz(@PathVariable Long courseId, @RequestBody Quiz quiz) {
        Quiz savedQuiz = quizService.createQuiz(courseId, quiz);
        return ResponseEntity.ok(savedQuiz);
    }

    // Update Quiz
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<Quiz> updateQuiz(@PathVariable Long id, @RequestBody Quiz quiz) {
        return ResponseEntity.ok(quizService.updateQuiz(id, quiz));
    }

    // Delete Quiz
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TUTOR')")
    public ResponseEntity<?> deleteQuiz(@PathVariable Long id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.ok().build();
    }

    // ----------- Read (accessible à tous) ------------

    // Get all quizzes
    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }
    // Get quiz by id
    @GetMapping("/{id}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    // Optional: get quizzes by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<Quiz>> getQuizzesByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(quizService.getQuizzesByCourse(courseId));
    }
    // Récupérer toutes les questions d'un quiz
    @GetMapping("/{quizId}/questions")
    public ResponseEntity<List<Question>> getQuizQuestions(@PathVariable Long quizId) {
        return ResponseEntity.ok(quizService.getQuizQuestions(quizId));
    }

    // Récupérer toutes les réponses d'une question
    @GetMapping("/questions/{questionId}/answers")
    public ResponseEntity<List<Answer>> getAnswerByQuizQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(quizService.getAnswerByQuizQuestion(questionId));
    }
    @PostMapping("/quiz/{quizId}")
    public ResponseEntity<Question> createQuestion(
            @PathVariable Long quizId,
            @RequestBody Question question) {

        Question savedQuestion = questionService.createQuestion(quizId, question);
        return ResponseEntity.ok(savedQuestion);
    }

    // Endpoint pour mettre à jour une question avec ses réponses
    @PutMapping("/question/{questionId}")
    public ResponseEntity<Question> updateQuestion(
            @PathVariable Long questionId,
            @RequestBody Question question) {

        Question updated = questionService.updateQuestion(questionId, question);
        return ResponseEntity.ok(updated);
    }
    @PostMapping("/quizAddQuestion/{quizId}")
    public ResponseEntity<Question> addQuestion(
            @PathVariable Long quizId,
            @RequestBody Question question) {
        Question saved = questionService.addQuestion(quizId, question);
        return ResponseEntity.ok(saved);
    }

    // 🗑 Supprimer une question
    @DeleteMapping("/question/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }

    // ➕ Ajouter une réponse à une question
    @PostMapping("/{questionId}/answers")
    public ResponseEntity<Answer> addAnswer(
            @PathVariable Long questionId,
            @RequestBody Answer answer) {
        Answer saved = questionService.addAnswer(questionId, answer);
        return ResponseEntity.ok(saved);
    }

    // ✏️ Modifier une réponse
    @PutMapping("/answers/{answerId}")
    public ResponseEntity<Answer> updateAnswer(
            @PathVariable Long answerId,
            @RequestBody Answer answerDetails) {
        Answer updated = questionService.updateAnswer(answerId, answerDetails);
        return ResponseEntity.ok(updated);
    }

    // 🗑 Supprimer une réponse
    @DeleteMapping("/answers/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Long answerId) {
        questionService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}