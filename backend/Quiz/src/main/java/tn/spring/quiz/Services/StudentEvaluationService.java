package tn.spring.quiz.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.StudentEvaluationRequest;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Models.QuizAttempt;
import tn.spring.quiz.Models.StudentEvaluation;
import tn.spring.quiz.Repositories.QuizAttemptRepository;
import tn.spring.quiz.Repositories.StudentEvaluationRepository;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentEvaluationService {

    private final StudentEvaluationRepository evaluationRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional(readOnly = true)
    public List<StudentEvaluation> getEvaluations(UUID studentId) {
        if (studentId == null) {
            return evaluationRepository.findAllByOrderByCreatedAtDesc();
        }
        return evaluationRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentEvaluation> getEvaluationsForStudentEmail(String studentEmail) {
        return evaluationRepository.findByStudentEmailIgnoreCaseOrderByCreatedAtDesc(normalizeEmail(studentEmail));
    }

    @Transactional(readOnly = true)
    public StudentEvaluation getEvaluation(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evaluation not found"));
    }

    public StudentEvaluation createEvaluation(StudentEvaluationRequest request, Authentication authentication) {
        StudentEvaluation evaluation = new StudentEvaluation();
        applyRequest(evaluation, request, authentication);
        return evaluationRepository.save(evaluation);
    }

    public StudentEvaluation updateEvaluation(Long id, StudentEvaluationRequest request, Authentication authentication) {
        StudentEvaluation evaluation = getEvaluation(id);
        applyRequest(evaluation, request, authentication);
        return evaluationRepository.save(evaluation);
    }

    public void deleteEvaluation(Long id) {
        StudentEvaluation evaluation = getEvaluation(id);
        evaluationRepository.delete(evaluation);
    }

    private void applyRequest(StudentEvaluation evaluation,
                              StudentEvaluationRequest request,
                              Authentication authentication) {
        evaluation.setStudentId(request.getStudentId());
        evaluation.setStudentName(requireText(request.getStudentName(), "Student name is required"));
        evaluation.setStudentEmail(normalizeEmail(request.getStudentEmail()));
        evaluation.setTitle(requireText(request.getTitle(), "Evaluation title is required"));
        evaluation.setFeedback(requireText(request.getFeedback(), "Feedback is required"));
        evaluation.setStrengths(trimToNull(request.getStrengths()));
        evaluation.setAreasToImprove(trimToNull(request.getAreasToImprove()));
        evaluation.setRecommendedActions(trimToNull(request.getRecommendedActions()));
        evaluation.setRating(request.getRating());
        evaluation.setEvaluatorEmail(authentication != null ? authentication.getName() : "system");
        evaluation.setEvaluatorRole(extractRole(authentication));

        if (request.getQuizAttemptId() == null) {
            clearAttemptSnapshot(evaluation);
            return;
        }

        QuizAttempt attempt = quizAttemptRepository.findById(request.getQuizAttemptId())
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        if (!attempt.getStudentId().equals(request.getStudentId())) {
            throw new RuntimeException("The selected quiz attempt does not belong to the chosen student");
        }

        Quiz quiz = attempt.getQuiz();
        evaluation.setQuizAttemptId(attempt.getId());
        evaluation.setQuizId(quiz != null ? quiz.getId() : null);
        evaluation.setQuizTitle(quiz != null ? quiz.getTitle() : null);
        evaluation.setScoreSnapshot(attempt.getScore());
        evaluation.setPassedSnapshot(attempt.getPassed());
    }

    private void clearAttemptSnapshot(StudentEvaluation evaluation) {
        evaluation.setQuizAttemptId(null);
        evaluation.setQuizId(null);
        evaluation.setQuizTitle(null);
        evaluation.setScoreSnapshot(null);
        evaluation.setPassedSnapshot(null);
    }

    private String extractRole(Authentication authentication) {
        if (authentication == null) {
            return "SYSTEM";
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .findFirst()
                .orElse("USER");
    }

    private String requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new RuntimeException(message);
        }
        return value.trim();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Student email is required");
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
