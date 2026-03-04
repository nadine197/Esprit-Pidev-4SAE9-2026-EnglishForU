package tn.spring.quiz.DTO;

import lombok.Data;

@Data
public class CertificateRequest {
    private Long courseId;       // id du cours
    private String studentId;    // UUID de l'étudiant en string
    private String userEmail;    // email saisi
    private String userName;     // nom affiché sur le certificat
    private Integer finalScore;  // score final
}