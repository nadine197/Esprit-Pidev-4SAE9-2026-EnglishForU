package tn.spring.packagee.DTOs;


import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateSubscriptionRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long packageOfferId;

    // optional
    private Long paymentId;

}