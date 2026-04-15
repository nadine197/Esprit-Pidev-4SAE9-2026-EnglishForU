package tn.spring.appointment.Repositories;

import org.springframework.data.domain.Page; // ✅ IMPORT CRUCIAL
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.spring.appointment.Enums.ApptStatus;
import tn.spring.appointment.Enums.LocationType;
import tn.spring.appointment.Models.Appointment;

import java.util.Optional;
import java.util.UUID;

public interface ApptRepository extends JpaRepository<Appointment, UUID> {
    @Query("SELECT a FROM Appointment a WHERE " +
            "(:search IS NULL OR LOWER(a.visitorName) LIKE :search OR LOWER(a.visitorEmail) LIKE :search) " +
            "AND (:status IS NULL OR a.status = :status) " +
            "AND (:location IS NULL OR a.locationType = :location) " +
            "AND (:suspicious IS NULL OR " +
            "     (:suspicious = true AND a.tabSwitchCount > 0) OR " +
            "     (:suspicious = false AND (a.tabSwitchCount IS NULL OR a.tabSwitchCount = 0)))")
    Page<Appointment> findAllWithFilters(
            @Param("search") String search, // Arrive déjà en minuscules avec les %
            @Param("status") ApptStatus status,
            @Param("location") LocationType location,
            @Param("suspicious") Boolean suspicious,
            Pageable pageable);

    Optional<Appointment> findByVisitorEmailAndAccessCode(String visitorEmail, String accessCode);

    // Spring Data utilisera cet import pour créer une réponse paginée
    Page<Appointment> findByVisitorNameContainingIgnoreCaseOrVisitorEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );


}