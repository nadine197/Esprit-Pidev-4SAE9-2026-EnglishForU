package tn.spring.packagee.DTOs;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ApplyPromoResponse {
    private final boolean valid;
    private String promoCode;
    private BigDecimal discountAmount;
    private BigDecimal amountFinal;

    public ApplyPromoResponse(boolean valid, String promoCode, BigDecimal discountAmount, BigDecimal amountFinal) {
        this.valid = valid;
        this.promoCode = promoCode;
        this.discountAmount = discountAmount;
        this.amountFinal = amountFinal;
    }

}