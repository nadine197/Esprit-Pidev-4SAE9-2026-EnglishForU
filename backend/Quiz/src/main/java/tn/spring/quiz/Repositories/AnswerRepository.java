package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.Answer;

import java.util.List;

public interface AnswerRepository  extends JpaRepository<Answer, Long> {
    List<Answer> findAllByQuestionIdOrderByIdAsc(Long questionId);

    @Transactional
    void deleteAllByQuestionId(Long questionId);
}