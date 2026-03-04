package tn.spring.packagee.DTOs;


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
    private Instant createdAt;
    private List<PackageItemResponse> items;

    public static class PackageItemResponse {
        private Long id;
        private String itemType;
        private Long itemId;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getItemType() { return itemType; }
        public void setItemType(String itemType) { this.itemType = itemType; }

        public Long getItemId() { return itemId; }
        public void setItemId(Long itemId) { this.itemId = itemId; }
    }

}
