package tn.spring.clubevent.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.spring.clubevent.Enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_participations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventParticipation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String userId;

    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private LocalDateTime requestedAt;

    private String stripePaymentIntentId;
    private Double transactionLatitude;
    private Double transactionLongitude;
    private LocalDateTime paidAt;
    private Double amountPaid;
    private String passCode;

    @PrePersist
    void prePersist() {
        requestedAt = LocalDateTime.now();
    }
}

