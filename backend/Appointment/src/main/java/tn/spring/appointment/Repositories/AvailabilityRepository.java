package tn.spring.appointment.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.appointment.Models.Availability;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByIsBookedFalse(); // For the visitor to see
    // Add this method to your repository
    List<Availability> findByIsBookedFalseAndStartTimeAfterOrderByStartTimeAsc(LocalDateTime now);
}
