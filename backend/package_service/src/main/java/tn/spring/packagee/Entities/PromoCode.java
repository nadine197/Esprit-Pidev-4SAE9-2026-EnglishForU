package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import tn.spring.packagee.Enum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "promo_code",
        uniqueConstraints = @UniqueConstraint(name = "uk_promo_code_code", columnNames = "code"),
        indexes = @Index(name = "idx_promo_code_active_end", columnList = "active,endDate")
)
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private DiscountType discountType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountValue;

    private LocalDate startDate;
    private LocalDate endDate;

    // global usage limit
    private Integer usageLimit;

    // per user usage limit
    private Integer usagePerUserLimit;

    @Column(precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(nullable = false)
    private Boolean active = true;

    // getters/setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public DiscountType getDiscountType() { return discountType; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getUsageLimit() { return usageLimit; }
    public void setUsageLimit(Integer usageLimit) { this.usageLimit = usageLimit; }

    public Integer getUsagePerUserLimit() { return usagePerUserLimit; }
    public void setUsagePerUserLimit(Integer usagePerUserLimit) { this.usagePerUserLimit = usagePerUserLimit; }

    public BigDecimal getMinAmount() { return minAmount; }
    public void setMinAmount(BigDecimal minAmount) { this.minAmount = minAmount; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}