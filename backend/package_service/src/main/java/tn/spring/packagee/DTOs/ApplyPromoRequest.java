package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.AppliedToType;

import java.math.BigDecimal;

@Setter
@Getter
public class ApplyPromoRequest {

    @NotBlank
    private String code;

    @NotNull
    private Long studentId;



    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal amountOriginal;

}