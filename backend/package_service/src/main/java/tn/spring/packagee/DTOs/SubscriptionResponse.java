package tn.spring.packagee.DTOs;



import lombok.Getter;
import lombok.Setter;
import tn.spring.packagee.Enum.SubscriptionStatus;

import java.time.LocalDate;

@Setter
@Getter
public class SubscriptionResponse {
    private Long id;
    private Long studentId;
    private Long packageOfferId;
    private LocalDate startDate;
    private LocalDate endDate;
    private SubscriptionStatus status;
    private Integer remainingUses;
    private Long paymentId;

}