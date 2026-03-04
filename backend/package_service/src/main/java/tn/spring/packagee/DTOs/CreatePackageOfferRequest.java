package tn.spring.packagee.DTOs;



import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PackageType;

import java.math.BigDecimal;

@Setter
@Getter
public class CreatePackageOfferRequest {

    @NotBlank @Size(max = 120)
    private String name;

    @Size(max = 1000)
    private String description;

    @NotNull
    private PackageType type;

    @NotNull @Min(1)
    private Integer durationDays;

    @NotNull @DecimalMin(value = "0.00", inclusive = false)
    private BigDecimal price;

    private Boolean isActive = true;

}