package tn.spring.packagee.DTOs;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FlouciVerifyDTO {
    private Boolean success;
    private String status; // SUCCESS / PENDING / EXPIRED / FAILURE
}