package tn.spring.quiz.Services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.CertificateGenerationRequest;
import tn.spring.quiz.DTO.QuizSubmissionRequest;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Certificate;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Models.QuizAttempt;
import tn.spring.quiz.Repositories.CertificateRepository;
import tn.spring.quiz.Repositories.QuizAttemptRepository;
import tn.spring.quiz.Repositories.QuizRepository;

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
    private final CertificatePdfService certificatePdfService;
    private final EmailService emailService;
    private final QrCodeService qrCodeService;

    @Value("${app.certificate.public-base-url:http://localhost:8056}")
    private String certificatePublicBaseUrl;

    @Transactional
    public QuizAttempt submitQuiz(QuizSubmissionRequest request) {

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        int totalQuestions = quiz.getQuestions().size();
        int correctAnswers = 0;

        for (Question question : quiz.getQuestions()) {
            Long selectedAnswerId = request.getAnswers().get(question.getId());
            if (selectedAnswerId == null) {
                continue;
            }

            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(selectedAnswerId) && answer.isCorrect()) {
                    correctAnswers++;
                    break;
                }
            }
        }

        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        boolean passedQuiz = score >= quiz.getPassingScore();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudentId(request.getStudentId());
        attempt.setScore(score);
        attempt.setPassed(passedQuiz);
        attempt.setSubmittedAt(LocalDateTime.now());

        attemptRepository.save(attempt);

        if (passedQuiz && quiz.getCourse() != null) {
            checkCourseCompletion(quiz.getCourse(), request.getStudentId());
        }

        return attempt;
    }

    @Transactional
    public byte[] generateAndEmailCourseCertificate(CertificateGenerationRequest request) {
        if (request.getQuizId() == null) {
            throw new RuntimeException("Quiz id is required.");
        }
        if (request.getStudentId() == null) {
            throw new RuntimeException("Student id is required.");
        }

        String studentEmail = normalizeEmail(request.getStudentEmail());
        if (studentEmail.isBlank()) {
            throw new RuntimeException("Student email is required to send the certificate.");
        }

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new RuntimeException("Quiz not found"));

        ensureQuizWasPassed(quiz.getId(), request.getStudentId());

        Certificate certificate = prepareCertificate(
                quiz,
                request.getStudentId(),
                request.getStudentName(),
                studentEmail
        );

        byte[] pdfBytes = certificatePdfService.buildCourseCertificatePdf(certificate, quiz.getTitle());
        String downloadUrl = buildDownloadUrl(certificate.getId());
        byte[] qrCodeBytes = qrCodeService.generatePng(downloadUrl, 320);

        try {
            emailService.sendCertificateEmail(
                    studentEmail,
                    certificate.getStudentName(),
                    certificate.getCourse().getTitle(),
                    downloadUrl,
                    pdfBytes,
                    qrCodeBytes,
                    buildCertificateFileName(certificate.getCourse())
            );
        } catch (MessagingException exception) {
            throw new RuntimeException("The certificate was generated, but the email could not be sent. Please verify the SMTP configuration and retry.", exception);
        }

        return pdfBytes;
    }

    @Transactional(readOnly = true)
    public byte[] downloadCertificatePdf(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found."));

        return certificatePdfService.buildCourseCertificatePdf(certificate, "Course Completion Assessment");
    }

    @Transactional(readOnly = true)
    public String buildCertificateDownloadFileName(Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found."));

        return buildCertificateFileName(certificate.getCourse());
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getQuizAttemptsStatus(Long quizId, UUID studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStudentId(quizId, studentId);

        Map<String, Object> result = new HashMap<>();
        result.put("totalAttempts", attempts.size());
        result.put("passed", attempts.stream().anyMatch(QuizAttempt::getPassed));
        result.put("attempts", attempts);

        return result;
    }

    private Certificate prepareCertificate(Quiz quiz, UUID studentId, String studentName, String studentEmail) {
        Course course = quiz.getCourse();
        if (course == null) {
            throw new RuntimeException("This quiz is not linked to a course yet.");
        }

        int finalScore = Math.max(calculateBestCourseScore(course, studentId), quiz.getPassingScore());

        Certificate certificate = certificateRepository.findByCourseAndStudentId(course, studentId)
                .orElseGet(Certificate::new);

        certificate.setCourse(course);
        certificate.setStudentId(studentId);
        certificate.setStudentName(normalizeStudentName(studentName, studentId));
        certificate.setStudentEmail(normalizeEmail(studentEmail));
        certificate.setFinalScore(Math.max(finalScore, certificate.getFinalScore() != null ? certificate.getFinalScore() : 0));
        certificate.setIssueDate(certificate.getIssueDate() != null ? certificate.getIssueDate() : LocalDate.now());

        return certificateRepository.save(certificate);
    }

    private void ensureQuizWasPassed(Long quizId, UUID studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        if (attempts.isEmpty()) {
            throw new RuntimeException("Take and pass this quiz before generating a certificate.");
        }

        boolean passed = attempts.stream().anyMatch(QuizAttempt::getPassed);
        if (!passed) {
            throw new RuntimeException("You need a score of 70% or more before generating a certificate.");
        }
    }

    private void checkCourseCompletion(Course course, UUID studentId) {
        if (course == null || course.getQuizzes() == null || course.getQuizzes().isEmpty()) {
            return;
        }

        long passedCount = course.getQuizzes().stream()
                .filter(q -> attemptRepository.existsByQuizIdAndStudentIdAndPassedTrue(q.getId(), studentId))
                .count();

        if (passedCount < course.getQuizzes().size()) {
            return;
        }

        int finalScore = calculateBestCourseScore(course, studentId);
        if (finalScore < 70 || certificateRepository.existsByCourseAndStudentId(course, studentId)) {
            return;
        }

        Certificate certificate = new Certificate();
        certificate.setCourse(course);
        certificate.setStudentId(studentId);
        certificate.setStudentName(normalizeStudentName(null, studentId));
        certificate.setStudentEmail("");
        certificate.setFinalScore(finalScore);
        certificate.setIssueDate(LocalDate.now());
        certificateRepository.save(certificate);
    }

    private int calculateBestCourseScore(Course course, UUID studentId) {
        List<QuizAttempt> attempts = attemptRepository.findByCourseIdAndStudentId(course.getCourseid(), studentId);
        if (attempts.isEmpty()) {
            return 0;
        }

        Map<Long, Integer> bestScoresByQuiz = new HashMap<>();
        for (QuizAttempt attempt : attempts) {
            if (attempt.getQuiz() == null || attempt.getQuiz().getId() == null || attempt.getScore() == null) {
                continue;
            }

            bestScoresByQuiz.merge(attempt.getQuiz().getId(), attempt.getScore(), Math::max);
        }

        return (int) Math.round(
                bestScoresByQuiz.values().stream()
                        .mapToInt(Integer::intValue)
                        .average()
                        .orElse(0)
        );
    }

    private String buildDownloadUrl(Long certificateId) {
        String baseUrl = certificatePublicBaseUrl != null ? certificatePublicBaseUrl.trim() : "";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + "/api/quiz-attempts/certificates/" + certificateId + "/pdf";
    }

    private String buildCertificateFileName(Course course) {
        String courseTitle = course != null && course.getTitle() != null ? course.getTitle() : "course-certificate";
        String slug = courseTitle.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        return (slug.isBlank() ? "course-certificate" : slug) + ".pdf";
    }

    private String normalizeStudentName(String studentName, UUID studentId) {
        if (studentName != null && !studentName.isBlank()) {
            return studentName.trim();
        }

        String shortId = studentId.toString();
        shortId = shortId.length() >= 8 ? shortId.substring(0, 8).toUpperCase() : shortId;
        return "Student " + shortId;
    }

    private String normalizeEmail(String email) {
        return email != null ? email.trim() : "";
    }
}
