package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TargetType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(
        name = "payment",
        indexes = {
                @Index(name = "idx_payment_student", columnList = "studentId"),
                @Index(name = "idx_payment_target", columnList = "targetType,targetId")
        }
)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID studentId;
    private String studentFullName;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountOriginal;

    private String checkoutUrl;

     private String providerRef;
    ;
    private String voucherNumber;     // ex: VCH-2026-0000007
    private Instant confirmedAt;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountFinal;



    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TargetType targetType;

    @Column(nullable = false)
    private Long targetId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // getters/setters

}