package tn.spring.quiz.Services;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.DTO.CertificateGenerationRequest;
import tn.spring.quiz.DTO.QuizAttemptAnswerResponse;
import tn.spring.quiz.DTO.QuizAttemptDetailsResponse;
import tn.spring.quiz.DTO.QuizAttemptOverviewResponse;
import tn.spring.quiz.DTO.QuizSubmissionRequest;
import tn.spring.quiz.Models.Answer;
import tn.spring.quiz.Models.Certificate;
import tn.spring.quiz.Models.Course;
import tn.spring.quiz.Models.Question;
import tn.spring.quiz.Models.Quiz;
import tn.spring.quiz.Models.QuizAttempt;
import tn.spring.quiz.Models.QuizAttemptAnswer;
import tn.spring.quiz.Repositories.CertificateRepository;
import tn.spring.quiz.Repositories.QuizAttemptRepository;
import tn.spring.quiz.Repositories.QuizRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private static final DateTimeFormatter CERTIFICATE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

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
        Map<Long, Long> submittedAnswers = request.getAnswers() != null ? request.getAnswers() : Map.of();
        List<QuizAttemptAnswer> attemptAnswers = new java.util.ArrayList<>();

        for (Question question : quiz.getQuestions()) {
            Long selectedAnswerId = submittedAnswers.get(question.getId());
            Answer selectedAnswer = null;
            Answer correctAnswer = null;
            boolean isCorrect = false;

            for (Answer answer : question.getAnswers()) {
                if (answer.isCorrect()) {
                    correctAnswer = answer;
                }
                if (selectedAnswerId != null && answer.getId().equals(selectedAnswerId)) {
                    selectedAnswer = answer;
                }
            }

            if (selectedAnswer != null && selectedAnswer.isCorrect()) {
                correctAnswers++;
                isCorrect = true;
            }

            QuizAttemptAnswer attemptAnswer = new QuizAttemptAnswer();
            attemptAnswer.setQuestionId(question.getId());
            attemptAnswer.setQuestionText(question.getText());
            attemptAnswer.setSelectedAnswerId(selectedAnswer != null ? selectedAnswer.getId() : null);
            attemptAnswer.setSelectedAnswerText(selectedAnswer != null ? selectedAnswer.getText() : null);
            attemptAnswer.setCorrectAnswerId(correctAnswer != null ? correctAnswer.getId() : null);
            attemptAnswer.setCorrectAnswerText(correctAnswer != null ? correctAnswer.getText() : null);
            attemptAnswer.setAnswered(selectedAnswer != null);
            attemptAnswer.setCorrect(isCorrect);
            attemptAnswers.add(attemptAnswer);
        }

        int score = totalQuestions > 0 ? (correctAnswers * 100) / totalQuestions : 0;
        boolean passedQuiz = score >= quiz.getPassingScore();

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setStudentId(request.getStudentId());
        attempt.setStudentName(normalizeStudentName(request.getStudentName(), request.getStudentId()));
        attempt.setStudentEmail(normalizeAttemptEmail(request.getStudentEmail()));
        attempt.setScore(score);
        attempt.setPassed(passedQuiz);
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.getAttemptAnswers().clear();

        for (QuizAttemptAnswer attemptAnswer : attemptAnswers) {
            attemptAnswer.setQuizAttempt(attempt);
            attempt.getAttemptAnswers().add(attemptAnswer);
        }

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

        String downloadUrl = buildDownloadUrl(certificate.getId());
        String qrContent = buildCertificateQrContent(certificate, quiz.getTitle());
        byte[] qrCodeBytes = qrCodeService.generatePng(qrContent, 320);
        byte[] pdfBytes = certificatePdfService.buildCourseCertificatePdf(
                certificate,
                quiz.getTitle(),
                null,
                qrCodeBytes
        );

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

        String qrContent = buildCertificateQrContent(certificate, "Course Completion Assessment");
        byte[] qrCodeBytes = qrCodeService.generatePng(qrContent, 320);

        return certificatePdfService.buildCourseCertificatePdf(
                certificate,
                "Course Completion Assessment",
                null,
                qrCodeBytes
        );
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

    @Transactional(readOnly = true)
    public List<QuizAttemptOverviewResponse> getAttemptOverview(UUID studentId) {
        List<QuizAttempt> attempts = studentId == null
                ? attemptRepository.findAllByOrderBySubmittedAtDesc()
                : attemptRepository.findAllByStudentIdOrderBySubmittedAtDesc(studentId);

        return attempts.stream()
                .map(this::toAttemptOverview)
                .toList();
    }

    @Transactional(readOnly = true)
    public QuizAttemptDetailsResponse getAttemptDetails(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Quiz attempt not found"));

        return toAttemptDetails(attempt);
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

    private String buildCertificateQrContent(Certificate certificate, String assessmentLabel) {
        Course course = certificate.getCourse();
        String courseTitle = course != null && course.getTitle() != null && !course.getTitle().isBlank()
                ? course.getTitle().trim()
                : "Untitled Course";
        String studentName = certificate.getStudentName() != null && !certificate.getStudentName().isBlank()
                ? certificate.getStudentName().trim()
                : normalizeStudentName(null, certificate.getStudentId());
        String studentEmail = certificate.getStudentEmail() != null && !certificate.getStudentEmail().isBlank()
                ? certificate.getStudentEmail().trim()
                : "Not provided";
        String issueDate = certificate.getIssueDate() != null
                ? certificate.getIssueDate().format(CERTIFICATE_DATE_FORMAT)
                : "Not specified";
        String safeAssessmentLabel = assessmentLabel != null && !assessmentLabel.isBlank()
                ? assessmentLabel.trim()
                : "Course Completion Assessment";

        return String.join("\n",
                "Certificate Information",
                "Certificate ID: CERT-" + certificate.getId(),
                "Student: " + studentName,
                "Email: " + studentEmail,
                "Course: " + courseTitle,
                "Assessment: " + safeAssessmentLabel,
                "Final Score: " + certificate.getFinalScore() + "%",
                "Issue Date: " + issueDate
        );
    }

    private String normalizeAttemptEmail(String email) {
        String normalized = normalizeEmail(email);
        return normalized.isBlank() ? "" : normalized.toLowerCase(Locale.ROOT);
    }

    private QuizAttemptOverviewResponse toAttemptOverview(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();

        return QuizAttemptOverviewResponse.builder()
                .id(attempt.getId())
                .studentId(attempt.getStudentId())
                .studentName(attempt.getStudentName())
                .studentEmail(attempt.getStudentEmail())
                .quizId(quiz != null ? quiz.getId() : null)
                .quizTitle(quiz != null ? quiz.getTitle() : null)
                .score(attempt.getScore())
                .passed(attempt.getPassed())
                .submittedAt(attempt.getSubmittedAt())
                .build();
    }

    private QuizAttemptDetailsResponse toAttemptDetails(QuizAttempt attempt) {
        Quiz quiz = attempt.getQuiz();

        return QuizAttemptDetailsResponse.builder()
                .id(attempt.getId())
                .studentId(attempt.getStudentId())
                .studentName(attempt.getStudentName())
                .studentEmail(attempt.getStudentEmail())
                .quizId(quiz != null ? quiz.getId() : null)
                .quizTitle(quiz != null ? quiz.getTitle() : null)
                .score(attempt.getScore())
                .passed(attempt.getPassed())
                .submittedAt(attempt.getSubmittedAt())
                .answers(attempt.getAttemptAnswers().stream()
                        .map(this::toAttemptAnswerResponse)
                        .toList())
                .build();
    }

    private QuizAttemptAnswerResponse toAttemptAnswerResponse(QuizAttemptAnswer attemptAnswer) {
        return QuizAttemptAnswerResponse.builder()
                .id(attemptAnswer.getId())
                .questionId(attemptAnswer.getQuestionId())
                .questionText(attemptAnswer.getQuestionText())
                .selectedAnswerId(attemptAnswer.getSelectedAnswerId())
                .selectedAnswerText(attemptAnswer.getSelectedAnswerText())
                .correctAnswerId(attemptAnswer.getCorrectAnswerId())
                .correctAnswerText(attemptAnswer.getCorrectAnswerText())
                .answered(attemptAnswer.getAnswered())
                .correct(attemptAnswer.getCorrect())
                .build();
    }
}
