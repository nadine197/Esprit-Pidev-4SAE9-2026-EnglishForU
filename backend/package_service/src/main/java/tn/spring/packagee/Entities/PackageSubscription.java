package tn.spring.packagee.Entities;

import jakarta.persistence.*;
import tn.spring.packagee.Enum.SubscriptionStatus;

import java.time.LocalDate;

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

    // payment-service reference (optional)
    private Long paymentId;

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getPackageOfferId() { return packageOfferId; }
    public void setPackageOfferId(Long packageOfferId) { this.packageOfferId = packageOfferId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public SubscriptionStatus getStatus() { return status; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }

    public Integer getRemainingUses() { return remainingUses; }
    public void setRemainingUses(Integer remainingUses) { this.remainingUses = remainingUses; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
}