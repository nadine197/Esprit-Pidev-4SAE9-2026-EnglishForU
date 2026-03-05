package tn.spring.packagee.DTOs;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PaymentMethod;
import tn.spring.packagee.Enum.TargetType;


import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class CreatePaymentRequest {
    @NotBlank
    @Email
    private String studentEmail;

    @NotNull
    private TargetType targetType;

    @NotNull
    private Long targetId;

    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amountOriginal;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull
    private PaymentMethod paymentMethod;



}