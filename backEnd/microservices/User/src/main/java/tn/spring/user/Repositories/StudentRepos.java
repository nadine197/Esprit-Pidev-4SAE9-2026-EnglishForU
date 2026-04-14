package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.Student;

import java.util.UUID;

public interface StudentRepos extends JpaRepository<Student, UUID> {
}

