package tn.spring.appointment.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.spring.appointment.Models.DiscussionGroup;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<DiscussionGroup, UUID> {
    @Query(value = "SELECT g.* FROM discussion_group g " +
            "LEFT JOIN discussion_group_student_ids s ON g.id = s.discussion_group_id " +
            "WHERE g.tutor_id = :userId OR s.student_ids = :userId",
            nativeQuery = true)
    List<DiscussionGroup> findGroupsByMemberId(@Param("userId") String userId);
    List<DiscussionGroup> findByTutorIdOrStudentIdsContaining(String tutorId, String studentId);
}