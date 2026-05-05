package tn.spring.course.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.course.Models.Course;
import java.util.List;
public interface CourseRepository extends JpaRepository<Course, Integer> {
    List<Course> findAllByOrderByDurationAsc();
    List<Course> findByTitleContainingIgnoreCase(String title);
    List<Course> findAll();

}