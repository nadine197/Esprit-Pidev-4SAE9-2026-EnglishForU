package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.PaymentMethod;

@Setter
@Getter
public class ConfirmPaymentRequest {


    private PaymentMethod provider;


    @NotBlank
    private String providerRef;



}