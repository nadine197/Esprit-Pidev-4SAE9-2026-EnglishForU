package tn.spring.clubevent.Models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tn.spring.clubevent.Enums.RequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "club_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMembership {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long clubId;

    @Column(nullable = false)
    private String userId;

    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status;

    private LocalDateTime requestedAt;

    @PrePersist
    void prePersist() {
        requestedAt = LocalDateTime.now();
    }
}

