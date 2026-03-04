package tn.spring.quiz.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.quiz.DTO.QuizSubmissionRequest;
import tn.spring.quiz.Services.QuizAttemptService;

import java.util.UUID;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@RequestBody QuizSubmissionRequest request) {
        return ResponseEntity.ok(
                attemptService.submitQuiz(request)
        );
    }
    @GetMapping("/status")
    public ResponseEntity<?> getQuizAttemptsStatus(
            @RequestParam Long quizId,
            @RequestParam UUID studentId
    ) {
        return ResponseEntity.ok(attemptService.getQuizAttemptsStatus(quizId, studentId));
    }
}