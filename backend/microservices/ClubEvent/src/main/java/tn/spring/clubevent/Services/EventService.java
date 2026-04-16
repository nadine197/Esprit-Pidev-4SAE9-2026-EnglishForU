package tn.spring.clubevent.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.clubevent.Enums.RequestStatus;
import tn.spring.clubevent.Models.Club;
import tn.spring.clubevent.Models.Event;
import tn.spring.clubevent.Models.EventParticipation;
import tn.spring.clubevent.Repositories.ClubRepository;
import tn.spring.clubevent.Repositories.EventParticipationRepository;
import tn.spring.clubevent.Repositories.EventRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final EventParticipationRepository participationRepository;
    private final ClubRepository clubRepository;

    public List<Map<String, Object>> getAllEvents(String currentUserId) {
        List<Event> events = eventRepository.findAll();
        return events.stream().map(e -> enrichEvent(e, currentUserId)).toList();
    }

    public List<Map<String, Object>> getEventsByClub(Long clubId, String currentUserId) {
        List<Event> events = eventRepository.findByClubId(clubId);
        return events.stream().map(e -> enrichEvent(e, currentUserId)).toList();
    }

    public Map<String, Object> getEventById(Long id, String currentUserId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        return enrichEvent(event, currentUserId);
    }

    public Event createEvent(Event event, String userId) {
        Club club = clubRepository.findById(event.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the club president can create events");
        }
        event.setClubName(club.getName());
        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event updated, String userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Club club = clubRepository.findById(event.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the club president can update events");
        }
        event.setTitle(updated.getTitle());
        event.setDescription(updated.getDescription());
        event.setImageUrl(updated.getImageUrl());
        event.setEventDate(updated.getEventDate());
        event.setLocation(updated.getLocation());
        event.setLatitude(updated.getLatitude());
        event.setLongitude(updated.getLongitude());
        event.setLocationName(updated.getLocationName());
        event.setPaid(updated.isPaid());
        event.setPrice(updated.getPrice());
        event.setMaxParticipants(updated.getMaxParticipants());
        return eventRepository.save(event);
    }

    public void deleteEvent(Long id, String userId) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Club club = clubRepository.findById(event.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the club president can delete events");
        }
        participationRepository.deleteAll(participationRepository.findByEventId(id));
        eventRepository.delete(event);
    }

    public EventParticipation requestParticipation(Long eventId, String userId, String userName) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        if (event.isPaid()) {
            throw new RuntimeException("This is a paid event. Please use the payment flow to register.");
        }
        if (participationRepository.findByEventIdAndUserId(eventId, userId).isPresent()) {
            throw new RuntimeException("Already requested or participating");
        }
        if (event.getMaxParticipants() != null) {
            long accepted = participationRepository.countByEventIdAndStatus(eventId, RequestStatus.ACCEPTED);
            if (accepted >= event.getMaxParticipants()) {
                throw new RuntimeException("Event is full. No more spots available.");
            }
        }
        EventParticipation participation = EventParticipation.builder()
                .eventId(eventId)
                .userId(userId)
                .userName(userName)
                .status(RequestStatus.PENDING)
                .build();
        return participationRepository.save(participation);
    }

    public EventParticipation acceptParticipant(Long eventId, Long participationId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Club club = clubRepository.findById(event.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the club president can accept participants");
        }
        EventParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        p.setStatus(RequestStatus.ACCEPTED);
        return participationRepository.save(p);
    }

    public EventParticipation rejectParticipant(Long eventId, Long participationId, String userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        Club club = clubRepository.findById(event.getClubId())
                .orElseThrow(() -> new RuntimeException("Club not found"));
        if (!club.getPresidentId().equals(userId)) {
            throw new RuntimeException("Only the club president can reject participants");
        }
        EventParticipation p = participationRepository.findById(participationId)
                .orElseThrow(() -> new RuntimeException("Participation not found"));
        p.setStatus(RequestStatus.REJECTED);
        return participationRepository.save(p);
    }

    public List<EventParticipation> getParticipants(Long eventId) {
        return participationRepository.findByEventIdAndStatus(eventId, RequestStatus.ACCEPTED);
    }

    public List<EventParticipation> getPendingParticipants(Long eventId) {
        return participationRepository.findByEventIdAndStatus(eventId, RequestStatus.PENDING);
    }

    private Map<String, Object> enrichEvent(Event event, String currentUserId) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", event.getId());
        map.put("title", event.getTitle());
        map.put("description", event.getDescription());
        map.put("imageUrl", event.getImageUrl());
        map.put("eventDate", event.getEventDate());
        map.put("location", event.getLocation());
        map.put("latitude", event.getLatitude());
        map.put("longitude", event.getLongitude());
        map.put("locationName", event.getLocationName());
        map.put("paid", event.isPaid());
        map.put("price", event.getPrice());
        map.put("maxParticipants", event.getMaxParticipants());
        map.put("clubId", event.getClubId());
        map.put("clubName", event.getClubName());
        map.put("createdAt", event.getCreatedAt());
        long acceptedCount = participationRepository.countByEventIdAndStatus(event.getId(), RequestStatus.ACCEPTED);
        map.put("participantCount", acceptedCount);
        if (event.getMaxParticipants() != null) {
            long spotsLeft = event.getMaxParticipants() - acceptedCount;
            map.put("spotsLeft", Math.max(0, spotsLeft));
        } else {
            map.put("spotsLeft", null);
        }
        Club club = clubRepository.findById(event.getClubId()).orElse(null);
        map.put("isClubPresident", club != null && currentUserId != null && club.getPresidentId().equals(currentUserId));
        if (currentUserId != null) {
            EventParticipation p = participationRepository.findByEventIdAndUserId(event.getId(), currentUserId).orElse(null);
            map.put("participationStatus", p != null ? p.getStatus().name() : null);
            map.put("participationId", p != null ? p.getId() : null);
        }
        return map;
    }
}

