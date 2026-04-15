package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Certificate;
import tn.spring.quiz.Models.Course;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    boolean existsByCourseAndStudentId(Course course, UUID studentId);
    Optional<Certificate> findByCourseAndStudentId(Course course, UUID studentId);
}
