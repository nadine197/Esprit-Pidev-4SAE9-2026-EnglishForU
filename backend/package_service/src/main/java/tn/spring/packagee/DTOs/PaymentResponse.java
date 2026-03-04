package tn.spring.packagee.DTOs;


import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Enum.PaymentStatus;
import tn.spring.packagee.Enum.TargetType;

import java.math.BigDecimal;
import java.time.Instant;

@Setter
@Getter
public class PaymentResponse {
    private Long id;
    private Long studentId;
    private TargetType targetType;
    private Long targetId;

    private BigDecimal amountOriginal;
    private BigDecimal discountAmount;
    private BigDecimal amountFinal;

    private PaymentStatus status;
    private PaymentMethod paymentMethod;
    private Instant createdAt;

}