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
import tn.spring.appointment.Models.Appointment;
import tn.spring.appointment.Models.Availability;
import tn.spring.appointment.Services.ApptService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Map;


@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class ApptController {

    private final ApptService service;

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
            @RequestParam(defaultValue = "appointmentDate,desc") String sort) {

        // 1. On découpe "appointmentDate,asc" en un tableau ["appointmentDate", "asc"]
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];

        // 2. On détermine la direction (par défaut DESC si non précisé ou incorrect)
        Sort.Direction direction = Sort.Direction.DESC;
        if (sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc")) {
            direction = Sort.Direction.ASC;
        }

        // 3. On crée l'objet Pageable avec le tri REEL
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));

        // 4. On passe ce pageable au service
        return ResponseEntity.ok(service.findAllPaged(search, pageable));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<Appointment> acceptAppointment(@PathVariable UUID id) {
        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.CONFIRMED, null, null));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Appointment> cancel(@PathVariable UUID id) {
        // AJOUTEZ UN 4ème PARAMÈTRE 'null' ICI
        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.CANCELLED, null, null));
    }

    // Remplacez vos anciennes méthodes "complete" par cette version unique
    @PutMapping("/{id}/complete")
    public ResponseEntity<Appointment> complete(
            @PathVariable UUID id,
            @RequestParam String result,
            @RequestParam(required = false) String score) { // 'required = false' permet de ne pas planter si le score est absent

        return ResponseEntity.ok(service.updateStatus(id, ApptStatus.COMPLETED, result, score));
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