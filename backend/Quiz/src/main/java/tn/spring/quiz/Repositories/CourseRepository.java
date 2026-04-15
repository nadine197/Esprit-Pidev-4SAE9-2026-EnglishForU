package tn.spring.quiz.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.quiz.Models.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
}
