package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Certificate;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    boolean existsByCourseAndStudentId(Long courseId, UUID studentId);
    Certificate findByCourseAndStudentId(Long courseId, UUID studentId);
    Optional<Certificate> findByCourse_CourseidAndStudentId(Long courseId, UUID studentId);
}