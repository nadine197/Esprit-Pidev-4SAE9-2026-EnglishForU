package tn.spring.packagee.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StripeCheckoutRequest {
    private Long paymentId;
    private Long amount;
    private String packageName;
}
