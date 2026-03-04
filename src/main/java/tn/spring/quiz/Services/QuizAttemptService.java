package tn.spring.quiz.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.QuizSubmissionRequest;
import tn.spring.quiz.Models.*;
import tn.spring.quiz.Repositories.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final QuizRepository quizRepository;
    private final QuizAttemptRepository attemptRepository;
    private final CertificateRepository certificateRepository;

    @Transactional
    public QuizAttempt submitQuiz(QuizSubmissionRequest request) {

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        // Calcul du score
        for (Question question : quiz.getQuestions()) {
            Long selectedAnswerId = request.getAnswers().get(question.getId());
            if (selectedAnswerId == null) continue;

            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(selectedAnswerId) && answer.isCorrect()) {
                    correctAnswers++;
                    break;
                }
            }
        }

        int score = (totalQuestions > 0) ? (correctAnswers * 100) / totalQuestions : 0;
        boolean passedQuiz = score >= quiz.getPassingScore();

        // Enregistrement de la tentative
        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudentId(request.getStudentId());
        attempt.setScore(score);
        attempt.setPassed(passedQuiz);
        attempt.setSubmittedAt(LocalDateTime.now());

        attemptRepository.save(attempt);

        // Vérifier si l’étudiant a réussi le cours
        if (passedQuiz && quiz.getCourse() != null) {
            checkCourseCompletion(quiz.getCourse(), request.getStudentId());
        }

        return attempt;
    }

    private void checkCourseCompletion(Course course, UUID studentId) {

        if (course == null) return;

        List<Quiz> quizzes = course.getQuizzes();
        if (quizzes.isEmpty()) return;

        long passedCount = quizzes.stream()
                .filter(q -> attemptRepository.existsByQuizIdAndStudentIdAndPassedTrue(q.getId(), studentId))
                .count();

        // Si pas tous les quiz sont réussis
        if (passedCount < quizzes.size()) return;

        // Calcul de la moyenne du cours
        List<QuizAttempt> attempts = attemptRepository.findByCourseIdAndStudentId(course.getCourseid(), studentId);
        double avgScore = attempts.stream()
                .mapToInt(QuizAttempt::getScore)
                .average()
                .orElse(0);

        // Créer le certificat si réussi et non déjà existant
        if (avgScore >= 70 &&
                !certificateRepository.existsByCourseAndStudentId(course.getCourseid(), studentId)) {

            Certificate cert = new Certificate();
            cert.setCourse(course);
            cert.setStudentId(studentId);
            cert.setFinalScore((int) avgScore);
            cert.setIssueDate(LocalDate.now());

            certificateRepository.save(cert);
        }
    }
    @Transactional(readOnly = true)
    public Map<String, Object> getQuizAttemptsStatus(Long quizId, UUID studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStudentId(quizId, studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAttempts", attempts.size());

        // Si l'étudiant a réussi au moins une fois
        boolean passed = attempts.stream().anyMatch(QuizAttempt::getPassed);
        result.put("passed", passed);

        // Ajouter éventuellement les scores de chaque tentative
        result.put("attempts", attempts);

        return result;
    }
}