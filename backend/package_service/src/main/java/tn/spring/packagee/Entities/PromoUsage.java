package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.AppliedToType;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
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

}