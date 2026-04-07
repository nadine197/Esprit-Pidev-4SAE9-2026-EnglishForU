package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PackageType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Setter
@Getter
public class PackageOfferResponse {
    private Long id;
    private String name;
    private String description;
    private PackageType type;
    private Integer durationDays;
    private BigDecimal price;
    private Boolean isActive;
    private List<String> features;
    private Instant createdAt;
    private List<PackageItemResponse> items;

    @Setter
    @Getter
    public static class PackageItemResponse {
        private Long id;
        private String itemType;
        private Long itemId;

    }

}
