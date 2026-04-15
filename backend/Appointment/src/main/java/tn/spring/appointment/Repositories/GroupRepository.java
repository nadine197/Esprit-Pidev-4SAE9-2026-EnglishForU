package tn.spring.appointment.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.appointment.Models.DiscussionGroup;
import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<DiscussionGroup, UUID> {

    // Cette méthode remplace l'ancienne qui posait problème
    List<DiscussionGroup> findByTutorEmailOrStudentEmailsContaining(String email1, String email2);
}