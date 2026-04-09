package tn.spring.user.DTOs;

import lombok.Data;
import tn.spring.user.Enums.ReportCategory;
import tn.spring.user.Enums.ReportSeverity;
import tn.spring.user.Enums.ReportStatus;

import java.util.UUID;

@Data
public class UpdateReportRequest {
    private String title;
    private ReportCategory category;
    private ReportSeverity severity;
    private String description;
    private String stepsToReproduce;
    private String expectedResult;
    private String actualResult;
    private String pageUrl;
    private String userAgent;
    private String appVersion;

    private ReportStatus status;
    private UUID assignedToUserId;
    private Boolean unassign;
    private Boolean assignToMe;
    private String requestInfoMessage;
}
