package tn.spring.clubevent.Controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.clubevent.Models.Feedback;
import tn.spring.clubevent.Services.FeedbackService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, Object> body,
                                            @RequestParam String userId,
                                            @RequestParam(required = false) String userName) {
        try {
            String targetType = (String) body.get("targetType");
            Long targetId = Long.valueOf(body.get("targetId").toString());
            int rating = Integer.parseInt(body.get("rating").toString());
            String comment = (String) body.getOrDefault("comment", null);
            String name = userName != null ? userName : userId;
            Feedback saved = feedbackService.submitFeedback(targetType, targetId, userId, name, rating, comment);
            return ResponseEntity.ok(saved);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getEventFeedback(@PathVariable Long eventId,
                                              @RequestParam(required = false) String userId) {
        List<Feedback> reviews = feedbackService.getFeedback("EVENT", eventId);
        Optional<Feedback> myReview = userId != null
                ? feedbackService.getMyFeedback("EVENT", eventId, userId)
                : Optional.empty();
        boolean canReview = feedbackService.canReviewEvent(eventId, userId) && myReview.isEmpty();

        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("myReview", myReview.orElse(null));
        result.put("canReview", canReview);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<?> getEventStats(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getStats("EVENT", eventId));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<?> getClubFeedback(@PathVariable Long clubId,
                                             @RequestParam(required = false) String userId) {
        List<Feedback> reviews = feedbackService.getFeedback("CLUB", clubId);
        Optional<Feedback> myReview = userId != null
                ? feedbackService.getMyFeedback("CLUB", clubId, userId)
                : Optional.empty();
        boolean canReview = feedbackService.canReviewClub(clubId, userId) && myReview.isEmpty();

        Map<String, Object> result = new HashMap<>();
        result.put("reviews", reviews);
        result.put("myReview", myReview.orElse(null));
        result.put("canReview", canReview);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/club/{clubId}/stats")
    public ResponseEntity<?> getClubStats(@PathVariable Long clubId) {
        return ResponseEntity.ok(feedbackService.getClubStats(clubId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id,
                                            @RequestParam String userId) {
        try {
            feedbackService.deleteMyFeedback(id, userId);
            return ResponseEntity.ok(Map.of("message", "Feedback deleted"));
        } catch (Exception e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        }
    }
}
