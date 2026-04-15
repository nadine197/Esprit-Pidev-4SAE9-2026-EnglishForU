package tn.spring.packagee.DTOs;

import lombok.Getter;

import java.math.BigDecimal;

public record ApplyPromoResponse
        (boolean valid, String promoCode, BigDecimal discountAmount, BigDecimal amountFinal) {

}