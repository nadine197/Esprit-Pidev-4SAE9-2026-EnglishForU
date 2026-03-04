package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Question;

public interface QuestionRepository extends JpaRepository<Question, Long> {
}