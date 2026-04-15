package tn.spring.clubevent.Services;

import org.springframework.stereotype.Service;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.Event;
import tn.spring.clubevent.Models.Feedback;
import tn.spring.clubevent.Models.EventParticipation;
import tn.spring.clubevent.Repositories.ClubMembershipRepository;
import tn.spring.clubevent.Repositories.EventParticipationRepository;
import tn.spring.clubevent.Repositories.EventRepository;
import tn.spring.clubevent.Repositories.FeedbackRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final EventParticipationRepository participationRepository;
    private final ClubMembershipRepository membershipRepository;
    private final EventRepository eventRepository;

    public FeedbackService(FeedbackRepository feedbackRepository,
                           EventParticipationRepository participationRepository,
                           ClubMembershipRepository membershipRepository,
                           EventRepository eventRepository) {
        this.feedbackRepository = feedbackRepository;
        this.participationRepository = participationRepository;
        this.membershipRepository = membershipRepository;
        this.eventRepository = eventRepository;
    }

    public Feedback submitFeedback(String targetType, Long targetId, String userId, String userName,
                                   int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        if (feedbackRepository.existsByTargetTypeAndTargetIdAndUserId(targetType, targetId, userId)) {
            throw new IllegalStateException("You have already submitted feedback for this " + targetType.toLowerCase());
        }

        if ("EVENT".equals(targetType)) {
            Event event = eventRepository.findById(targetId)
                    .orElseThrow(() -> new NoSuchElementException("Event not found"));
            if (event.getEventDate() != null && event.getEventDate().isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("You can only review an event after it has taken place");
            }
            Optional<EventParticipation> participation = participationRepository.findByEventIdAndUserId(targetId, userId);
            boolean eligible = participation.isPresent() &&
                    (RequestStatus.ACCEPTED.equals(participation.get().getStatus()) ||
                            participation.get().getAmountPaid() != null);
            if (!eligible) {
                throw new IllegalStateException("Only accepted participants can review this event");
            }
        } else if ("CLUB".equals(targetType)) {
            boolean isMember = membershipRepository.findByClubIdAndUserId(targetId, userId)
                    .map(m -> RequestStatus.ACCEPTED.equals(m.getStatus()))
                    .orElse(false);
            if (!isMember) {
                throw new IllegalStateException("Only accepted club members can review this club");
            }
        } else {
            throw new IllegalArgumentException("Invalid targetType: " + targetType);
        }

        Feedback feedback = new Feedback();
        feedback.setTargetType(targetType);
        feedback.setTargetId(targetId);
        feedback.setUserId(userId);
        feedback.setUserName(userName);
        feedback.setRating(rating);
        feedback.setComment(comment);
        return feedbackRepository.save(feedback);
    }

    public List<Feedback> getFeedback(String targetType, Long targetId) {
        return feedbackRepository.findByTargetTypeAndTargetIdOrderByCreatedAtDesc(targetType, targetId);
    }

    public Optional<Feedback> getMyFeedback(String targetType, Long targetId, String userId) {
        return feedbackRepository.findByTargetTypeAndTargetIdAndUserId(targetType, targetId, userId);
    }

    public Map<String, Object> getStats(String targetType, Long targetId) {
        long total = feedbackRepository.countByTargetTypeAndTargetId(targetType, targetId);
        Double avg = feedbackRepository.avgRating(targetType, targetId);
        List<Object[]> dist = feedbackRepository.ratingDistribution(targetType, targetId);

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) distribution.put(i, 0L);
        for (Object[] row : dist) {
            distribution.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        long positive = distribution.get(4) + distribution.get(5);
        long neutral = distribution.get(3);
        long negative = distribution.get(1) + distribution.get(2);

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalReviews", total);
        stats.put("averageRating", avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0);
        stats.put("likePercent", total > 0 ? Math.round(positive * 1000.0 / total) / 10.0 : 0.0);
        stats.put("neutralPercent", total > 0 ? Math.round(neutral * 1000.0 / total) / 10.0 : 0.0);
        stats.put("dislikePercent", total > 0 ? Math.round(negative * 1000.0 / total) / 10.0 : 0.0);
        stats.put("distribution", distribution);
        return stats;
    }

    public Map<String, Object> getClubStats(Long clubId) {
        Map<String, Object> stats = getStats("CLUB", clubId);

        long memberCount = membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.ACCEPTED);
        long pendingCount = membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.PENDING);
        long rejectedCount = membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.REJECTED);
        long totalMemberships = memberCount + pendingCount + rejectedCount;

        stats.put("totalMembers", memberCount);
        stats.put("totalMemberships", totalMemberships);
        stats.put("acceptedPercent", totalMemberships > 0 ? Math.round(memberCount * 1000.0 / totalMemberships) / 10.0 : 0.0);
        stats.put("pendingPercent", totalMemberships > 0 ? Math.round(pendingCount * 1000.0 / totalMemberships) / 10.0 : 0.0);
        stats.put("rejectedPercent", totalMemberships > 0 ? Math.round(rejectedCount * 1000.0 / totalMemberships) / 10.0 : 0.0);

        List<Event> events = eventRepository.findByClubId(clubId);

        List<Map<String, Object>> perEventStats = new ArrayList<>();
        double totalEventRatings = 0;
        int ratedEvents = 0;
        long totalEventReviews = 0;
        double totalParticipationRate = 0;

        for (Event event : events) {
            Map<String, Object> evStats = getStats("EVENT", event.getId());
            Map<String, Object> evEntry = new LinkedHashMap<>();
            evEntry.put("eventId", event.getId());
            evEntry.put("eventTitle", event.getTitle());
            evEntry.put("eventDate", event.getEventDate());
            evEntry.put("averageRating", evStats.get("averageRating"));
            evEntry.put("totalReviews", evStats.get("totalReviews"));
            evEntry.put("likePercent", evStats.get("likePercent"));
            evEntry.put("dislikePercent", evStats.get("dislikePercent"));
            evEntry.put("neutralPercent", evStats.get("neutralPercent"));

            long participants = participationRepository.findByEventId(event.getId()).stream()
                    .filter(p -> RequestStatus.ACCEPTED.equals(p.getStatus()) || p.getAmountPaid() != null)
                    .count();
            double partRate = memberCount > 0 ? Math.round(participants * 1000.0 / memberCount) / 10.0 : 0.0;
            evEntry.put("participants", participants);
            evEntry.put("participationRate", partRate);
            totalParticipationRate += partRate;

            perEventStats.add(evEntry);

            double evAvg = (double) evStats.get("averageRating");
            long evTotal = (long) evStats.get("totalReviews");
            if (evTotal > 0) {
                totalEventRatings += evAvg;
                ratedEvents++;
                totalEventReviews += evTotal;
            }
        }

        stats.put("totalEvents", (long) events.size());
        stats.put("totalEventReviews", totalEventReviews);
        stats.put("overallEventAvg", ratedEvents > 0 ? Math.round(totalEventRatings / ratedEvents * 10.0) / 10.0 : 0.0);
        stats.put("averageParticipationRate", events.size() > 0 ? Math.round(totalParticipationRate / events.size() * 10.0) / 10.0 : 0.0);
        stats.put("perEventStats", perEventStats);
        return stats;
    }

    public void deleteMyFeedback(Long feedbackId, String userId) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new NoSuchElementException("Feedback not found"));
        if (!feedback.getUserId().equals(userId)) {
            throw new IllegalStateException("You can only delete your own feedback");
        }
        feedbackRepository.delete(feedback);
    }

    public boolean canReviewEvent(Long eventId, String userId) {
        if (userId == null) return false;
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return false;
        if (event.getEventDate() != null && event.getEventDate().isAfter(LocalDateTime.now())) return false;
        return participationRepository.findByEventIdAndUserId(eventId, userId)
                .map(p -> RequestStatus.ACCEPTED.equals(p.getStatus()) || p.getAmountPaid() != null)
                .orElse(false);
    }

    public boolean canReviewClub(Long clubId, String userId) {
        if (userId == null) return false;
        return membershipRepository.findByClubIdAndUserId(clubId, userId)
                .map(m -> RequestStatus.ACCEPTED.equals(m.getStatus()))
                .orElse(false);
    }
}
