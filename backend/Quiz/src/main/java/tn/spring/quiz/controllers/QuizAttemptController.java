package tn.spring.quiz.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.quiz.Services.QuizAttemptService;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    @PostMapping("/motivation-suggestions")
    public ResponseEntity<?> getMotivationSuggestion(@RequestBody Object payload) {
        // ✅ Appelle la méthode "generateMotivation" du service
        return ResponseEntity.ok(attemptService.generateMotivation(payload));
    }

    @GetMapping("/overview")
    public ResponseEntity<?> getOverview(@RequestParam String studentId) {
        // ✅ Appelle la méthode "getStudentOverview" du service
        return ResponseEntity.ok(attemptService.getStudentOverview(studentId));
    }
}