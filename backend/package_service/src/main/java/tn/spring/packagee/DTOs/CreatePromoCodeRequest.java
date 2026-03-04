package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
public class CreatePromoCodeRequest {

    @NotBlank @Size(max = 40)
    private String code;

    @NotNull
    private DiscountType discountType;

    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal discountValue;

    private LocalDate startDate;
    private LocalDate endDate;

    @Min(1)
    private Integer usageLimit;

    @Min(1)
    private Integer usagePerUserLimit;

    @DecimalMin(value = "0.00", inclusive = true)
    private BigDecimal minAmount;

    private Boolean active = true;

}