package tn.spring.clubevent.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.clubevent.Models.Event;
import tn.spring.clubevent.Models.EventParticipation;
import tn.spring.clubevent.Services.EventService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllEvents(@RequestParam(required = false) String userId) {
        return ResponseEntity.ok(eventService.getAllEvents(userId));
    }

    @GetMapping("/club/{clubId}")
    public ResponseEntity<List<Map<String, Object>>> getEventsByClub(@PathVariable Long clubId, @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(eventService.getEventsByClub(clubId, userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getEventById(@PathVariable Long id, @RequestParam(required = false) String userId) {
        return ResponseEntity.ok(eventService.getEventById(id, userId));
    }

    @PostMapping
    public ResponseEntity<Event> createEvent(@RequestBody Event event, @RequestParam String userId) {
        return ResponseEntity.ok(eventService.createEvent(event, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable Long id, @RequestBody Event event, @RequestParam String userId) {
        return ResponseEntity.ok(eventService.updateEvent(id, event, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id, @RequestParam String userId) {
        eventService.deleteEvent(id, userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/participate")
    public ResponseEntity<EventParticipation> participate(@PathVariable Long id, @RequestParam String userId, @RequestParam String userName) {
        return ResponseEntity.ok(eventService.requestParticipation(id, userId, userName));
    }

    @PutMapping("/{eventId}/participants/{participationId}/accept")
    public ResponseEntity<EventParticipation> acceptParticipant(@PathVariable Long eventId, @PathVariable Long participationId, @RequestParam String userId) {
        return ResponseEntity.ok(eventService.acceptParticipant(eventId, participationId, userId));
    }

    @PutMapping("/{eventId}/participants/{participationId}/reject")
    public ResponseEntity<EventParticipation> rejectParticipant(@PathVariable Long eventId, @PathVariable Long participationId, @RequestParam String userId) {
        return ResponseEntity.ok(eventService.rejectParticipant(eventId, participationId, userId));
    }

    @GetMapping("/{id}/participants")
    public ResponseEntity<List<EventParticipation>> getParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getParticipants(id));
    }

    @GetMapping("/{id}/participants/pending")
    public ResponseEntity<List<EventParticipation>> getPendingParticipants(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getPendingParticipants(id));
    }
}

