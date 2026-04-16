package tn.spring.clubevent.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.spring.clubevent.Models.Feedback;

import java.util.List;
import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, Long targetId);

    Optional<Feedback> findByTargetTypeAndTargetIdAndUserId(String targetType, Long targetId, String userId);

    boolean existsByTargetTypeAndTargetIdAndUserId(String targetType, Long targetId, String userId);

    long countByTargetTypeAndTargetId(String targetType, Long targetId);

    @Query("SELECT AVG(f.rating) FROM Feedback f WHERE f.targetType = :type AND f.targetId = :id")
    Double avgRating(@Param("type") String type, @Param("id") Long id);

    @Query("SELECT f.rating, COUNT(f) FROM Feedback f WHERE f.targetType = :type AND f.targetId = :id GROUP BY f.rating ORDER BY f.rating")
    List<Object[]> ratingDistribution(@Param("type") String type, @Param("id") Long id);
}
