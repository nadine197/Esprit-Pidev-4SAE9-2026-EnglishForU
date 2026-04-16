package tn.spring.clubevent.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.clubevent.Models.Club;
import tn.spring.clubevent.Models.ClubMembership;
import tn.spring.clubevent.Services.ClubService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllClubs(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(clubService.getAllClubs(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getClubById(@PathVariable Long id, @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(clubService.getClubById(id, userId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyClubs(@RequestParam String userId) {
        return ResponseEntity.ok(clubService.getMyClubs(userId));
    }

    @PostMapping
    public ResponseEntity<Club> createClub(@RequestBody Club club, @RequestParam String userId, @RequestParam String userName) {
        club.setPresidentId(userId);
        club.setPresidentName(userName);
        return ResponseEntity.ok(clubService.createClub(club));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Club> updateClub(@PathVariable Long id, @RequestBody Club club, @RequestParam String userId) {
        return ResponseEntity.ok(clubService.updateClub(id, club, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id, @RequestParam String userId) {
        clubService.deleteClub(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<ClubMembership> joinClub(@PathVariable Long id, @RequestParam String userId, @RequestParam String userName) {
        return ResponseEntity.ok(clubService.requestJoin(id, userId, userName));
    }

    @PutMapping("/{clubId}/members/{membershipId}/accept")
    public ResponseEntity<ClubMembership> acceptMember(@PathVariable Long clubId, @PathVariable Long membershipId, @RequestParam String userId) {
        return ResponseEntity.ok(clubService.acceptMember(clubId, membershipId, userId));
    }

    @PutMapping("/{clubId}/members/{membershipId}/reject")
    public ResponseEntity<ClubMembership> rejectMember(@PathVariable Long clubId, @PathVariable Long membershipId, @RequestParam String userId) {
        return ResponseEntity.ok(clubService.rejectMember(clubId, membershipId, userId));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<ClubMembership>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getMembers(id));
    }

    @GetMapping("/{id}/members/pending")
    public ResponseEntity<List<ClubMembership>> getPendingMembers(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getPendingMembers(id));
    }
}

