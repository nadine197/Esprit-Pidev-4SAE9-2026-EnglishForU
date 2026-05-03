package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Quiz;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
}