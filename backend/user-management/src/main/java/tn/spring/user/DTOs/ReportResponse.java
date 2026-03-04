package tn.spring.user.DTOs;

import lombok.Builder;
import lombok.Data;
import tn.spring.user.Enums.ReportCategory;
import tn.spring.user.Enums.ReportSeverity;
import tn.spring.user.Enums.ReportStatus;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReportResponse {
    private Long id;
    private String title;
    private ReportCategory category;
    private ReportSeverity severity;
    private ReportStatus status;
    private String description;
    private String shortDescription;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String pageUrl;
    private String userAgent;
    private String appVersion;
    private Instant createdAt;
    private Instant updatedAt;
    private UserSummary createdBy;
    private UserSummary assignedTo;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String lastName;
        private String email;
    }
}
