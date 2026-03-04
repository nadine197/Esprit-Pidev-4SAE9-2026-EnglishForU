package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
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

}