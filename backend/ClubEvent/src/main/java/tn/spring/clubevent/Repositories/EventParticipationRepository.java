package tn.spring.clubevent.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.EventParticipation;

import java.util.List;
import java.util.Optional;

public interface EventParticipationRepository extends JpaRepository<EventParticipation, Long> {
    List<EventParticipation> findByEventId(Long eventId);
    List<EventParticipation> findByEventIdAndStatus(Long eventId, RequestStatus status);
    List<EventParticipation> findByUserId(String userId);
    Optional<EventParticipation> findByEventIdAndUserId(Long eventId, String userId);
    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<EventParticipation> findByUserIdAndAmountPaidIsNotNullOrderByPaidAtDesc(String userId);

    Optional<EventParticipation> findByPassCode(String passCode);
}

