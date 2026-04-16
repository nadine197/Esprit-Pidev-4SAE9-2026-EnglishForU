package tn.spring.appointment.Controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.spring.appointment.Enums.ApptStatus;
import tn.spring.appointment.Enums.LocationType;
import tn.spring.appointment.Models.Appointment;
import tn.spring.appointment.Models.Availability;
import tn.spring.appointment.Services.ApptService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;


@RestController
@RequestMapping("/api/appointments")
public class ApptController {

    private final ApptService service;

    public ApptController(ApptService service) {
        this.service = service;
    }

    // --- SECTION VISITEUR ---

    @PostMapping("/book")
    public ResponseEntity<Appointment> book(@RequestBody Appointment appt) {
        return ResponseEntity.ok(service.book(appt));
    }

    @GetMapping("/available-slots")
    public List<Availability> getSlots() {
        return service.getAvailableSlots();
    }

    // --- SECTION ADMIN (Gestion des créneaux) ---

    @PostMapping("/slots")
    public ResponseEntity<Availability> addSlot(@RequestBody Availability slot) {
        return ResponseEntity.ok(service.addAvailability(slot));
    }


    @GetMapping("/all")
    public ResponseEntity<Page<Appointment>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) ApptStatus status,
            @RequestParam(required = false) LocationType locationType,
            @RequestParam(required = false) Boolean suspicious,
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {

        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc"))
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // ✅ CORRECTION : On passe tous les filtres au service
        return ResponseEntity.ok(service.findAllPagedAdvanced(search, status, locationType, suspicious, pageable));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable UUID id) {
        // AJOUTEZ UN 4ème PARAMÈTRE 'null' ICI
        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.CANCELLED, null, null, 0));
    }


    @PutMapping("/{id}/complete")
    public ResponseEntity<Appointment> complete(
            @PathVariable UUID id,
            @RequestParam(required = false, defaultValue = "Unknown") String result,
            @RequestParam(required = false, defaultValue = "0/0") String score,
            @RequestParam(required = false, defaultValue = "0") Integer cheatCount) {

        // On appelle le service avec les valeurs (qui seront soit les vraies, soit les défauts)
        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.COMPLETED, result, score, cheatCount));
    }

    // Pour les autres méthodes (Accept/Cancel), passe "0" pour le cheatCount
    @PutMapping("/{id}/accept")
    public ResponseEntity<Appointment> acceptAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.CONFIRMED, null, null, 0));
    }

    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Appointment> reschedule(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime newDate) {
        return ResponseEntity.ok(service.reschedule(id, newDate));
    }

    @PostMapping("/verify-access")
    public ResponseEntity<Appointment> verifyAccess(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");
        return ResponseEntity.ok(service.verifyAccess(email, code));
    }

}