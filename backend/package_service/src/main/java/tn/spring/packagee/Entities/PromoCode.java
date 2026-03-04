package tn.spring.packagee.Entities;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.DiscountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

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



    @Column(name = "current_uses", nullable = false)
    private int currentUses = 0;


    @Column(nullable = false)
    private Boolean active = true;
    @Column(name = "created_at", nullable = false)
    private Date createdAt ;




    @Size(max = 50)
    @Column(name = "for_user")
    private String forUser;
    // getters/setters

}