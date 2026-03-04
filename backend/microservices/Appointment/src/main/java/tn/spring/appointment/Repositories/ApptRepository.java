package tn.spring.appointment.Repositories;

import org.springframework.data.domain.Page; // ✅ IMPORT CRUCIAL
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.appointment.Models.Appointment;

import java.util.Optional;
import java.util.UUID;

public interface ApptRepository extends JpaRepository<Appointment, UUID> {
    Optional<Appointment> findByVisitorEmailAndAccessCode(String visitorEmail, String accessCode);

    // Spring Data utilisera cet import pour créer une réponse paginée
    Page<Appointment> findByVisitorNameContainingIgnoreCaseOrVisitorEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );


}