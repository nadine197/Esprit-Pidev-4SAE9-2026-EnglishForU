package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.user.Models.Tutor;
import tn.spring.user.Models.User;

import java.util.UUID;

public interface TutorRepos extends JpaRepository<Tutor, UUID> {
}
