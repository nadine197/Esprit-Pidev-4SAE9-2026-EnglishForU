package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.quiz.Models.Answer;

public interface AnswerRepository  extends JpaRepository<Answer, Long> {

    @Transactional
    void deleteAllByQuestionId(Long questionId);
}
