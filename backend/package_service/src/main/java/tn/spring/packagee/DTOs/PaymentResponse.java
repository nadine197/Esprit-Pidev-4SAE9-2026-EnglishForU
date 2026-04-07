package tn.spring.packagee.DTOs;


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
public class PaymentResponse {
    private Long id;
    private UUID studentId;
    private TargetType targetType;
    private Long targetId;
    private String studentFullName;
    private BigDecimal amountOriginal;
    private BigDecimal discountAmount;
    private BigDecimal amountFinal;
    private String providerRef;          // flouci payment_id / stripe session id
    private String checkoutUrl;
    private PaymentStatus status;
    private String voucherNumber;
    private Instant confirmedAt;
    private PaymentMethod paymentMethod;
    private Instant createdAt;

}