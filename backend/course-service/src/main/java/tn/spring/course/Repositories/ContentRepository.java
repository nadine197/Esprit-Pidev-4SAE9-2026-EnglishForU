package tn.spring.course.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.course.Models.Content;
import tn.spring.course.Models.Course;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface
ContentRepository extends JpaRepository<Content,Integer>{
    List<Content> findByTitleContainingIgnoreCase(String title);
    List<Content> findAll();
    @Query("SELECT c.type, COUNT(c) FROM Content c GROUP BY c.type")
    List<Object[]> countByType();

}
