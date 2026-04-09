package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.QuizAttempt;

import java.util.List;
import java.util.UUID;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    @Transactional
    void deleteAllByQuizId(Long quizId);

    boolean existsByQuizIdAndStudentIdAndPassedTrue(Long quizId, UUID studentId);

    @Query("""
        SELECT qa FROM QuizAttempt qa
        WHERE qa.quiz.course.courseid = :courseId
        AND qa.studentId = :studentId
    """)
    List<QuizAttempt> findByCourseIdAndStudentId(
            @Param("courseId") Long courseId,
            @Param("studentId") UUID studentId
    );

    @Query("""
        SELECT qa FROM QuizAttempt qa
        WHERE qa.quiz.id = :quizId
        AND qa.studentId = :studentId
        ORDER BY qa.submittedAt ASC
    """)
    List<QuizAttempt> findByQuizIdAndStudentId(
            @Param("quizId") Long quizId,
            @Param("studentId") UUID studentId
    );
}
