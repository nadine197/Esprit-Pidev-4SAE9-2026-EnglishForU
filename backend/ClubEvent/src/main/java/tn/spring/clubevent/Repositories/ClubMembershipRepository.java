package tn.spring.clubevent.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.ClubMembership;

import java.util.List;
import java.util.Optional;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {
    List<ClubMembership> findByClubId(Long clubId);
    List<ClubMembership> findByClubIdAndStatus(Long clubId, RequestStatus status);
    List<ClubMembership> findByUserId(String userId);
    List<ClubMembership> findByUserIdAndStatus(String userId, RequestStatus status);
    Optional<ClubMembership> findByClubIdAndUserId(Long clubId, String userId);
    long countByClubIdAndStatus(Long clubId, RequestStatus status);
}

