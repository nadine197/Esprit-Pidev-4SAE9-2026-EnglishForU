package tn.spring.appointment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.appointment.Enums.ApptStatus;
import tn.spring.appointment.Models.Appointment;
import tn.spring.appointment.Models.Availability;
import tn.spring.appointment.Repositories.ApptRepository;
import tn.spring.appointment.Services.ApptService;
import tn.spring.appointment.Services.NotificationService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApptServiceTest {

    @Mock private ApptRepository apptRepository;
    @Mock private NotificationService notificationService;
    @InjectMocks private ApptService apptService;

    @Test
    @DisplayName("Logique : Réservation avec génération de code 6 chiffres")
    void testBookGeneratesCode() {
        Appointment appt = new Appointment();
        when(apptRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Appointment saved = apptService.book(appt);

        assertNotNull(saved.getAccessCode());
        assertEquals(6, saved.getAccessCode().length());
        assertEquals(ApptStatus.PENDING, saved.getStatus());
        verify(apptRepository, times(1)).save(appt);
    }

    @Test
    @DisplayName("Logique Complexe : Mise à jour avec Score et CheatCount")
    void testUpdateStatusWithCheatData() {
        UUID id = UUID.randomUUID();
        Appointment mockAppt = Appointment.builder()
                .id(id).visitorName("Khalil").visitorEmail("k@test.com").build();

        when(apptRepository.findById(id)).thenReturn(Optional.of(mockAppt));
        when(apptRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // On simule : Niveau B2, Score 3/4, 5 sorties d'onglets
        Appointment result = apptService.updateStatus(id, ApptStatus.COMPLETED, "B2", "3/4", 5);

        assertEquals(ApptStatus.COMPLETED, result.getStatus());
        assertEquals(5, result.getTabSwitchCount());
        assertEquals("B2", result.getLevelResult());
        verify(notificationService).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("CRUD : Erreur si date de fin avant début")
    void testAddAvailabilityValidation() {
        Availability slot = new Availability();
        slot.setStartTime(LocalDateTime.now().plusDays(1));
        slot.setEndTime(LocalDateTime.now()); // Erreur ici

        assertThrows(ResponseStatusException.class, () -> apptService.addAvailability(slot));
    }
}