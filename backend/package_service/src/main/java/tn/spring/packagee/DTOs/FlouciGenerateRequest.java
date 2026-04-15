package tn.spring.packagee.DTOs;

// request sent to flouci

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FlouciGenerateRequest {
    private int amount;                 // millimes
    private String success_link;
    private String fail_link;
    private String webhook;             // optional
    private String developer_tracking_id;
    private Integer session_timeout_secs;
    private Boolean accept_card;
}