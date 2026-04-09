package tn.spring.quiz.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CertificateGenerationRequest {
    private Long quizId;
    private UUID studentId;
    private String studentName;
    private String studentEmail;
}
