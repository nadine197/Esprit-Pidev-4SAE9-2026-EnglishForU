package tn.spring.user.Models;

import jakarta.persistence.*;
import lombok.*;
import tn.spring.user.Enums.ReportActivityType;
import tn.spring.user.Enums.ReportStatus;

import java.time.Instant;

@Entity
@Table(name = "report_activities")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actorUser;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private ReportActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ReportStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private ReportStatus toStatus;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = Instant.now();
    }
}
