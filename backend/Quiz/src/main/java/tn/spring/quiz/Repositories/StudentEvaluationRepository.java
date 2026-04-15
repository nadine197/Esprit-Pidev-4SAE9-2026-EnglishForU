package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.StudentEvaluation;

import java.util.List;
import java.util.UUID;

public interface StudentEvaluationRepository extends JpaRepository<StudentEvaluation, Long> {
    List<StudentEvaluation> findAllByOrderByCreatedAtDesc();

    List<StudentEvaluation> findByStudentIdOrderByCreatedAtDesc(UUID studentId);

    List<StudentEvaluation> findByStudentEmailIgnoreCaseOrderByCreatedAtDesc(String studentEmail);
}
