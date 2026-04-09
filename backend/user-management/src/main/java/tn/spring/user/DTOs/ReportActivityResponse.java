package tn.spring.user.DTOs;

import lombok.Builder;
import lombok.Data;
import tn.spring.user.Enums.ReportActivityType;
import tn.spring.user.Enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReportActivityResponse {
    private Long id;
    private Long reportId;
    private ReportActivityType type;
    private ReportStatus fromStatus;
    private ReportStatus toStatus;
    private String details;
    private Instant createdAt;
    private UserSummary actor;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String lastName;
        private String email;
    }
}
