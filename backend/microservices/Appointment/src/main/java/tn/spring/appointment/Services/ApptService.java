package tn.spring.appointment.Services;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // ✅ Import correct
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.appointment.Enums.ApptStatus;
import tn.spring.appointment.Models.Appointment;
import tn.spring.appointment.Models.Availability;
import tn.spring.appointment.Models.DiscussionGroup;
import tn.spring.appointment.Repositories.ApptRepository;
import tn.spring.appointment.Repositories.AvailabilityRepository;
import tn.spring.appointment.Repositories.GroupRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Random;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApptService {

    private final ApptRepository apptRepository;
    private final AvailabilityRepository availabilityRepository;
    private final NotificationService notificationService;
    private final GroupRepository groupRepository;


    public Appointment book(Appointment appt) {
        appt.setStatus(ApptStatus.PENDING);

        // --- GÉNÉRATION DU CODE D'ACCÈS (6 chiffres) ---
        String code = String.format("%06d", new Random().nextInt(1000000));
        appt.setAccessCode(code);

        return apptRepository.save(appt);
    }
    // --- NOUVELLE MÉTHODE : VÉRIFICATION DU CODE ---
    public Appointment verifyAccess(String email, String code) {
        return apptRepository.findByVisitorEmailAndAccessCode(email, code)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Email or Access Code"));
    }

    public List<Availability> getAvailableSlots() {
        return availabilityRepository.findByIsBookedFalse();
    }

    public Availability addAvailability(Availability slot) {
        if (slot.getEndTime().isBefore(slot.getStartTime())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End time must be after start time");
        }
        slot.setIsBooked(false);
        return availabilityRepository.save(slot);
    }

    // --- MÉTHODE DE PAGINATION ---
    public Page<Appointment> findAllPaged(String search, Pageable pageable) {
        if (search == null || search.isEmpty()) {
            return apptRepository.findAll(pageable); // findAll(Pageable) renvoie déjà une Page
        }
        // Cette méthode appelle le repo et renvoie la Page<Appointment>
        return apptRepository.findByVisitorNameContainingIgnoreCaseOrVisitorEmailContainingIgnoreCase(search, search, pageable);
    }

    @Transactional
    public Appointment updateStatus(UUID id, ApptStatus status, String result, String score, Integer cheatCount) { // Remplacé int par Integer
        Appointment appt = apptRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        appt.setStatus(status);
        if (result != null) appt.setLevelResult(result);
        if (score != null) appt.setQcmScore(score);

        // --- ENREGISTREMENT DE LA SÉCURITÉ ---
        // Maintenant, la comparaison != null fonctionne car cheatCount est un Integer
        appt.setTabSwitchCount(cheatCount != null ? cheatCount : 0);

        Appointment saved = apptRepository.save(appt);

        String subject = "EnglishForU - Update on your Appointment";
        String message = "Hello " + appt.getVisitorName() + ",\n\n";

        if (status == ApptStatus.CONFIRMED) {
            message += "Your placement test is CONFIRMED.\n";
            message += "Mode: " + appt.getLocationType() + "\n";
            message += "Date: " + appt.getAppointmentDate() + "\n\n";
            message += "YOUR ACCESS CODE: " + appt.getAccessCode() + "\n";
            message += "Use this code on our homepage to start your test.";
        }
        else if (status == ApptStatus.COMPLETED) {
            message += "Congratulations! You have completed your test.\n";
            message += "Score: " + score + "\n";
            message += "Level: " + result + "\n";

            // Vérification sécurisée pour le message d'email
            if (cheatCount != null && cheatCount > 0) {
                message += "\nNote: Our system detected that you left the test page " + cheatCount + " time(s).";
            }
        }

        notificationService.sendEmail(appt.getVisitorEmail(), subject, message);
        return saved;
    }
        @Transactional
    public Appointment reschedule(UUID id, LocalDateTime newDate) {
        Appointment appt = apptRepository.findById(id).orElseThrow();
        appt.setAppointmentDate(newDate);
        Appointment saved = apptRepository.save(appt);

        // Notify of date change
        String text = "Your EnglishForU test has been rescheduled to: " + newDate;
        notificationService.sendEmail(appt.getVisitorEmail(), "Appointment Rescheduled", text);
        notificationService.sendSms(appt.getVisitorPhone(), text);

        return saved;
    }

    public List<DiscussionGroup> getGroupsByUserId(String userId) {
        return groupRepository.findGroupsByMemberId(userId);
    }
}