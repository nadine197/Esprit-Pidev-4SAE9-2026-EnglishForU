package tn.spring.packagee.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponsePaymentFlouciDTO {
    private String link;
    private String payment_id;
    private String developer_tracking_id;
    private Boolean success;
}
