package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TargetType;

import java.math.BigDecimal;
import java.time.Instant;

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
    private Long studentId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountOriginal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amountFinal;

    @Column(nullable = false, length = 8)
    private String currency = "TND";

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

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public BigDecimal getAmountOriginal() { return amountOriginal; }
    public void setAmountOriginal(BigDecimal amountOriginal) { this.amountOriginal = amountOriginal; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getAmountFinal() { return amountFinal; }
    public void setAmountFinal(BigDecimal amountFinal) { this.amountFinal = amountFinal; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }

    public TargetType getTargetType() { return targetType; }
    public void setTargetType(TargetType targetType) { this.targetType = targetType; }

    public Long getTargetId() { return targetId; }
    public void setTargetId(Long targetId) { this.targetId = targetId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}