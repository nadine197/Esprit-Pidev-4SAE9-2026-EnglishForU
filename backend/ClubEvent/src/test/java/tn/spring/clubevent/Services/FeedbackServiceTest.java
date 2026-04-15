package tn.spring.clubevent.Services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.Event;
import tn.spring.clubevent.Repositories.ClubMembershipRepository;
import tn.spring.clubevent.Repositories.EventParticipationRepository;
import tn.spring.clubevent.Repositories.EventRepository;
import tn.spring.clubevent.Repositories.FeedbackRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private EventParticipationRepository participationRepository;

    @Mock
    private ClubMembershipRepository membershipRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private FeedbackService feedbackService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetClubStats() {
        Long clubId = 1L;

        // Mock getting feedback stats for CLUB
        when(feedbackRepository.countByTargetTypeAndTargetId("CLUB", clubId)).thenReturn(5L);
        when(feedbackRepository.avgRating("CLUB", clubId)).thenReturn(4.2);
        when(feedbackRepository.ratingDistribution("CLUB", clubId)).thenReturn(new ArrayList<>());

        // Mock club memberships
        when(membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.ACCEPTED)).thenReturn(10L);
        when(membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.PENDING)).thenReturn(2L);
        when(membershipRepository.countByClubIdAndStatus(clubId, RequestStatus.REJECTED)).thenReturn(1L);

        // Mock events
        List<Event> events = new ArrayList<>();
        Event e1 = new Event();
        e1.setId(100L);
        e1.setTitle("Test Event");
        events.add(e1);
        when(eventRepository.findByClubId(clubId)).thenReturn(events);

        // Mock event feedback stats
        when(feedbackRepository.countByTargetTypeAndTargetId("EVENT", 100L)).thenReturn(0L);
        when(feedbackRepository.avgRating("EVENT", 100L)).thenReturn(0.0);
        when(feedbackRepository.ratingDistribution("EVENT", 100L)).thenReturn(new ArrayList<>());
        when(participationRepository.findByEventId(100L)).thenReturn(new ArrayList<>());

        // Execute
        Map<String, Object> stats = feedbackService.getClubStats(clubId);

        // Verify
        assertNotNull(stats);
        assertEquals(10L, stats.get("totalMembers"));
        assertEquals(13L, stats.get("totalMemberships"));
        // 10/13 = 76.9%
        assertEquals(76.9, stats.get("acceptedPercent"));
        // 5 total reviews
        assertEquals(5L, stats.get("totalReviews"));
        assertEquals(4.2, stats.get("averageRating"));
        assertEquals(1L, stats.get("totalEvents"));
    }
}
