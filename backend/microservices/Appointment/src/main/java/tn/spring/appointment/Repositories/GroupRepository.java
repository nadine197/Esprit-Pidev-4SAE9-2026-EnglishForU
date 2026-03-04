package tn.spring.appointment.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.appointment.Models.DiscussionGroup;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<DiscussionGroup, UUID> {
    // Trouve les groupes où l'utilisateur est le tuteur OU est dans la liste des étudiants
    List<DiscussionGroup> findByTutorIdOrStudentIdsContaining(String tutorId, String studentId);
}