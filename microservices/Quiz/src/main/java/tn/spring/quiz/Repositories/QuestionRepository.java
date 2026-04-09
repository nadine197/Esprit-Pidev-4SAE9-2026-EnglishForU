package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findAllByQuizIdOrderByIdAsc(Long quizId);
}