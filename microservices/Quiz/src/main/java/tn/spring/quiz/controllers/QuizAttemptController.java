package tn.spring.quiz.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.quiz.DTO.CertificateGenerationRequest;
import tn.spring.quiz.DTO.QuizSubmissionRequest;
import tn.spring.quiz.Services.QuizAttemptService;
import jakarta.validation.Valid;

import java.util.UUID;

@RestController
@RequestMapping("/api/quiz-attempts")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class QuizAttemptController {

    private final QuizAttemptService attemptService;

    @PostMapping("/submit")
    public ResponseEntity<?> submitQuiz(@Valid @RequestBody QuizSubmissionRequest request) {
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

    @PostMapping(value = "/certificates/generate", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> generateCourseCertificate(
            @RequestBody CertificateGenerationRequest request
    ) {
        byte[] pdfBytes = attemptService.generateAndEmailCourseCertificate(request);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"course-certificate.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @GetMapping(value = "/certificates/{certificateId}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadCertificatePdf(@PathVariable Long certificateId) {
        byte[] pdfBytes = attemptService.downloadCertificatePdf(certificateId);
        String fileName = attemptService.buildCertificateDownloadFileName(certificateId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
}
