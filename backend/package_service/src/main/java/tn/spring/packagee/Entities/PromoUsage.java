package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import tn.spring.packagee.Enum.AppliedToType;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "promo_usage",
        indexes = {
                @Index(name = "idx_promo_usage_promo", columnList = "promoCodeId"),
                @Index(name = "idx_promo_usage_student", columnList = "studentId")
        }
)
public class PromoUsage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long promoCodeId;

    @Column(nullable = false)
    private Long studentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AppliedToType appliedToType;

    @Column(nullable = false)
    private Long appliedToId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, updatable = false)
    private Instant usedAt = Instant.now();

    // payment-service reference
    private Long paymentId;

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPromoCodeId() { return promoCodeId; }
    public void setPromoCodeId(Long promoCodeId) { this.promoCodeId = promoCodeId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public AppliedToType getAppliedToType() { return appliedToType; }
    public void setAppliedToType(AppliedToType appliedToType) { this.appliedToType = appliedToType; }

    public Long getAppliedToId() { return appliedToId; }
    public void setAppliedToId(Long appliedToId) { this.appliedToId = appliedToId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public Instant getUsedAt() { return usedAt; }
    public void setUsedAt(Instant usedAt) { this.usedAt = usedAt; }

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
}