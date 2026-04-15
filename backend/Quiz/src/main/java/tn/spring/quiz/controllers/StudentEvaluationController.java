package tn.spring.quiz.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tn.spring.quiz.DTO.EvaluationMotivationSuggestionRequest;
import tn.spring.quiz.DTO.EvaluationMotivationSuggestionResponse;
import tn.spring.quiz.DTO.StudentEvaluationRequest;
import tn.spring.quiz.Models.StudentEvaluation;
import tn.spring.quiz.Services.GeminiEvaluationSuggestionService;
import tn.spring.quiz.Services.StudentEvaluationService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/evaluations")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class StudentEvaluationController {

    private final StudentEvaluationService evaluationService;
    private final GeminiEvaluationSuggestionService geminiEvaluationSuggestionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<List<StudentEvaluation>> getEvaluations(@RequestParam(required = false) UUID studentId) {
        return ResponseEntity.ok(evaluationService.getEvaluations(studentId));
    }

    @GetMapping("/mine")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<StudentEvaluation>> getMyEvaluations(Authentication authentication) {
        return ResponseEntity.ok(evaluationService.getEvaluationsForStudentEmail(authentication.getName()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<StudentEvaluation> getEvaluation(@PathVariable Long id) {
        return ResponseEntity.ok(evaluationService.getEvaluation(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<StudentEvaluation> createEvaluation(
            @Valid @RequestBody StudentEvaluationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(evaluationService.createEvaluation(request, authentication));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<StudentEvaluation> updateEvaluation(
            @PathVariable Long id,
            @Valid @RequestBody StudentEvaluationRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(evaluationService.updateEvaluation(id, request, authentication));
    }

    @PostMapping("/motivation-suggestions")
    public ResponseEntity<EvaluationMotivationSuggestionResponse> suggestMotivation(
            @Valid @RequestBody EvaluationMotivationSuggestionRequest request
    ) {
        return ResponseEntity.ok(geminiEvaluationSuggestionService.suggestMotivation(request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN','TUTOR')")
    public ResponseEntity<Void> deleteEvaluation(@PathVariable Long id) {
        evaluationService.deleteEvaluation(id);
        return ResponseEntity.noContent().build();
    }
}
