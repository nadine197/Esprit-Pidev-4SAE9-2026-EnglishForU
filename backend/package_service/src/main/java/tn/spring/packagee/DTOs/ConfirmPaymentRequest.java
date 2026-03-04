package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ConfirmPaymentRequest {

    @NotBlank
    private String provider;
    private Long promoCodeId;     // optional


    @NotBlank
    private String providerRef;

    private String responsePayload;

}