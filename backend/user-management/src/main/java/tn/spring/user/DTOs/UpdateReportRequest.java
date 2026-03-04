package tn.spring.user.DTOs;

import lombok.Data;
import tn.spring.user.Enums.ReportStatus;

import java.util.UUID;

@Data
public class UpdateReportRequest {
    private ReportStatus status;
    private UUID assignedToUserId;
    private Boolean unassign;
    private Boolean assignToMe;
}
