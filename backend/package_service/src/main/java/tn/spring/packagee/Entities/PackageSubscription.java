package tn.spring.packagee.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.SubscriptionStatus;

import java.time.LocalDate;

@Setter
@Getter
@Entity
@Table(
        name = "package_subscription",
        indexes = {
                @Index(name = "idx_subscription_student", columnList = "studentId"),
                @Index(name = "idx_subscription_package", columnList = "packageOfferId")
        }
)
public class PackageSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user-service id
    @Column(nullable = false)
    private Long studentId;

    // package-service internal id
    @Column(nullable = false)
    private Long packageOfferId;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    private Integer remainingUses;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="payment_id")
    private Payment payment;

    // getters/setters

}