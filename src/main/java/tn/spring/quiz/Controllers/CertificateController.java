package tn.spring.quiz.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import tn.spring.quiz.DTO.CertificateRequest;
import tn.spring.quiz.Models.Certificate;
import tn.spring.quiz.Repositories.CertificateRepository;
import tn.spring.quiz.Repositories.CourseRepository;
import tn.spring.quiz.Services.CertificatePdfService;
import tn.spring.quiz.Services.EmailService;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@RequiredArgsConstructor
@CrossOrigin("*")
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final CertificatePdfService pdfService;
    private final EmailService emailService;
    private final CourseRepository courseRepository;


    @PostMapping("/generate-and-send")
    public ResponseEntity<byte[]> generateAndSend(@RequestBody CertificateRequest request) throws Exception {

        // ✅ Créer un certificat directement au moment de la demande
        Certificate cert = new Certificate();
        cert.setCourse(courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found")));
        cert.setStudentId(UUID.fromString(request.getStudentId()));
        cert.setFinalScore(request.getFinalScore());
        cert.setIssueDate(LocalDate.now());

        certificateRepository.save(cert);

        // Sauvegarder dans la DB
        certificateRepository.save(cert);

        // Générer le PDF
        byte[] pdf = pdfService.generateCertificatePdf(cert, request.getUserName());

        // Envoyer le PDF par email
        emailService.sendCertificateEmail(request.getUserEmail(), pdf);

        // Retourner le PDF pour téléchargement direct depuis le frontend
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=certificate.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
    @GetMapping("/verify/{id}")
    public String verifyCertificate(@PathVariable Long id) {
        boolean exists = certificateRepository.existsById(id);

        if (exists) {
            return "✅ Certificate is VALID";
        } else {
            return "❌ Certificate NOT FOUND";
        }
    }
    @GetMapping("/by-course/{courseId}/student/{studentId}")
    public Certificate getCertificateByCourseAndStudent(
            @PathVariable Long courseId,
            @PathVariable UUID studentId) {

        return certificateRepository
                .findByCourse_CourseidAndStudentId(courseId, studentId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));
    }
}