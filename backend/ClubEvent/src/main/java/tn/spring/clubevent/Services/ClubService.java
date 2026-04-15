package tn.spring.clubevent.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.Club;
import tn.spring.clubevent.Models.ClubMembership;
import tn.spring.clubevent.Repositories.ClubMembershipRepository;
import tn.spring.clubevent.Repositories.ClubRepository;
import tn.spring.clubevent.Repositories.EventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMembershipRepository membershipRepository;
    private final EventRepository eventRepository;

    public List<Map<String, Object>> getAllClubs(String currentUserId) {
        List<Club> clubs = clubRepository.findAll();
        return clubs.stream().map(c -> enrichClub(c, currentUserId)).toList();
    }

    public Map<String, Object> getClubById(Long id, String currentUserId) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        return enrichClub(club, currentUserId);
    }

    public List<Map<String, Object>> getMyClubs(String userId) {
        List<Club> ownedClubs = clubRepository.findByPresidentId(userId);
        List<ClubMembership> memberships = membershipRepository.findByUserIdAndStatus(userId, RequestStatus.ACCEPTED);
        List<Long> memberClubIds = memberships.stream().map(ClubMembership::getClubId).toList();
        List<Club> memberClubs = memberClubIds.isEmpty() ? List.of() : clubRepository.findAllById(memberClubIds);

        java.util.Set<Long> seen = new java.util.HashSet<>();
        List<Club> all = new java.util.ArrayList<>();
        for (Club c : ownedClubs) { if (seen.add(c.getId())) all.add(c); }
        for (Club c : memberClubs) { if (seen.add(c.getId())) all.add(c); }

        return all.stream().map(c -> enrichClub(c, userId)).toList();
    }

    public Club createClub(Club club) {
        return clubRepository.save(club);
    }

    public Club updateClub(Long id, Club updated, String userId) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the president can update this club");
        }
        club.setName(updated.getName());
        club.setDescription(updated.getDescription());
        club.setImageUrl(updated.getImageUrl());
        return clubRepository.save(club);
    }

    public void deleteClub(Long id, String userId) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the president can delete this club");
        }
        membershipRepository.deleteAll(membershipRepository.findByClubId(id));
        eventRepository.deleteAll(eventRepository.findByClubId(id));
        clubRepository.delete(club);
    }

    public ClubMembership requestJoin(Long clubId, String userId, String userName) {
        clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (membershipRepository.findByClubIdAndUserId(clubId, userId).isPresent()) {
            throw new RuntimeException("Already requested or member");
        }
        ClubMembership membership = ClubMembership.builder()
                .clubId(clubId)
                .userId(userId)
                .userName(userName)
                .status(RequestStatus.PENDING)
                .build();
        return membershipRepository.save(membership);
    }

    public ClubMembership acceptMember(Long clubId, Long membershipId, String userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the president can accept members");
        }
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        membership.setStatus(RequestStatus.ACCEPTED);
        return membershipRepository.save(membership);
    }

    public ClubMembership rejectMember(Long clubId, Long membershipId, String userId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the president can reject members");
        }
        ClubMembership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new RuntimeException("Membership not found"));
        membership.setStatus(RequestStatus.REJECTED);
        return membershipRepository.save(membership);
    }

    public List<ClubMembership> getMembers(Long clubId) {
        return membershipRepository.findByClubIdAndStatus(clubId, RequestStatus.ACCEPTED);
    }

    public List<ClubMembership> getPendingMembers(Long clubId) {
        return membershipRepository.findByClubIdAndStatus(clubId, RequestStatus.PENDING);
    }

    private Map<String, Object> enrichClub(Club club, String currentUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", club.getId());
        map.put("name", club.getName());
        map.put("description", club.getDescription());
        map.put("imageUrl", club.getImageUrl());
        map.put("presidentId", club.getPresidentId());
        map.put("presidentName", club.getPresidentName());
        map.put("createdAt", club.getCreatedAt());
        map.put("memberCount", membershipRepository.countByClubIdAndStatus(club.getId(), RequestStatus.ACCEPTED));
        map.put("eventCount", eventRepository.findByClubId(club.getId()).size());
        if (currentUserId != null) {
            map.put("isPresident", club.getPresidentId().equals(currentUserId));
            ClubMembership m = membershipRepository.findByClubIdAndUserId(club.getId(), currentUserId).orElse(null);
            map.put("membershipStatus", m != null ? m.getStatus().name() : null);
            map.put("membershipId", m != null ? m.getId() : null);
        }
        return map;
    }
}

