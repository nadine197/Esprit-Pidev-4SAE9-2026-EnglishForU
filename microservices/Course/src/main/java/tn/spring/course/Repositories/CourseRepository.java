package tn.spring.course.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.course.Models.Course;

public interface CourseRepository extends JpaRepository<Course, Integer> {
}