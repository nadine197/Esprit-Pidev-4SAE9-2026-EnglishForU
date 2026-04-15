package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Content;

public interface ContentRepository  extends JpaRepository<Content, Long> {
}
