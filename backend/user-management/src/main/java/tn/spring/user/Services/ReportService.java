package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.CreateReportRequest;
import tn.spring.user.DTOs.NotificationPublishRequest;
import tn.spring.user.DTOs.ReportResponse;
import tn.spring.user.DTOs.UpdateReportRequest;
import tn.spring.user.Enums.NotificationType;
import tn.spring.user.Enums.ReportStatus;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.ReportRepo;
import tn.spring.user.Repositories.UserRepos;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private final ReportRepo reportRepo;
    private final NotificationPublisher notificationPublisher;
    private final UserRepos userRepos;

    public ReportResponse createReport(CreateReportRequest request, String currentUserEmail) {
        validateCreateReportRequest(request);

        User reporter = findUserByEmail(currentUserEmail);

        Report report = Report.builder()
                .createdByUser(reporter)
                .title(request.getTitle().trim())
                .category(request.getCategory())
                .severity(request.getSeverity())
                .description(request.getDescription().trim())
                .stepsToReproduce(nullableTrim(request.getStepsToReproduce()))
                .expectedResult(nullableTrim(request.getExpectedResult()))
                .actualResult(nullableTrim(request.getActualResult()))
                .pageUrl(nullableTrim(request.getPageUrl()))
                .userAgent(nullableTrim(request.getUserAgent()))
                .appVersion(nullableTrim(request.getAppVersion()))
                .status(ReportStatus.NEW)
                .assignedToUser(null)
                .build();

        Report savedReport = reportRepo.save(report);
        createHelpDeskNotifications(savedReport, reporter);

        return toReportResponse(savedReport);
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getMyReports(String currentUserEmail) {
        User reporter = findUserByEmail(currentUserEmail);
        return reportRepo.findByCreatedByUserOrderByCreatedAtDesc(reporter)
                .stream()
                .map(this::toReportResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportResponse> getHelpDeskReports(ReportStatus status) {
        List<Report> reports = (status == null)
                ? reportRepo.findAllByOrderByCreatedAtDesc()
                : reportRepo.findByStatusOrderByCreatedAtDesc(status);

        return reports.stream()
                .map(this::toReportResponse)
                .toList();
    }

    public ReportResponse updateReport(Long reportId, UpdateReportRequest request, String currentUserEmail) {
        Report report = reportRepo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND"));

        if (request.getTitle() != null) {
            String title = request.getTitle().trim();
            if (title.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TITLE_REQUIRED");
            }
            report.setTitle(title);
        }

        if (request.getCategory() != null) {
            report.setCategory(request.getCategory());
        }

        if (request.getSeverity() != null) {
            report.setSeverity(request.getSeverity());
        }

        if (request.getDescription() != null) {
            String description = request.getDescription().trim();
            if (description.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DESCRIPTION_REQUIRED");
            }
            report.setDescription(description);
        }

        if (request.getStepsToReproduce() != null) {
            report.setStepsToReproduce(nullableTrim(request.getStepsToReproduce()));
        }
        if (request.getExpectedResult() != null) {
            report.setExpectedResult(nullableTrim(request.getExpectedResult()));
        }
        if (request.getActualResult() != null) {
            report.setActualResult(nullableTrim(request.getActualResult()));
        }
        if (request.getPageUrl() != null) {
            report.setPageUrl(nullableTrim(request.getPageUrl()));
        }
        if (request.getUserAgent() != null) {
            report.setUserAgent(nullableTrim(request.getUserAgent()));
        }
        if (request.getAppVersion() != null) {
            report.setAppVersion(nullableTrim(request.getAppVersion()));
        }

        if (request.getStatus() != null) {
            report.setStatus(request.getStatus());
        }

        if (Boolean.TRUE.equals(request.getUnassign())) {
            report.setAssignedToUser(null);
        } else if (Boolean.TRUE.equals(request.getAssignToMe())) {
            User currentHelpDeskUser = findUserByEmail(currentUserEmail);
            report.setAssignedToUser(currentHelpDeskUser);
        } else if (request.getAssignedToUserId() != null) {
            User assignee = findUserById(request.getAssignedToUserId());
            report.setAssignedToUser(assignee);
        }

        Report updated = reportRepo.save(report);
        return toReportResponse(updated);
    }

    private void createHelpDeskNotifications(Report report, User reporter) {
        NotificationPublishRequest notificationRequest = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_CREATED)
                .title("New report submitted")
                .message("" + reporter.getName() + " " + reporter.getLastName() + " reported: " + report.getTitle())
                .link("/helpdesk/board?ticketId=" + report.getId())
                .reportId(report.getId())
                .build();

        notificationPublisher.publishToRole(UserRole.HELP_DESK, notificationRequest);
    }

    private User findUserByEmail(String email) {
        return userRepos.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND"));
    }

    private User findUserById(UUID userId) {
        return userRepos.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ASSIGNEE_NOT_FOUND"));
    }

    private void validateCreateReportRequest(CreateReportRequest request) {
        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "TITLE_REQUIRED");
        }
        if (request.getDescription() == null || request.getDescription().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DESCRIPTION_REQUIRED");
        }
        if (request.getCategory() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CATEGORY_REQUIRED");
        }
        if (request.getSeverity() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SEVERITY_REQUIRED");
        }
    }

    private String nullableTrim(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private ReportResponse toReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .category(report.getCategory())
                .severity(report.getSeverity())
                .status(report.getStatus())
                .description(report.getDescription())
                .shortDescription(toShortDescription(report.getDescription()))
                .stepsToReproduce(report.getStepsToReproduce())
                .expectedResult(report.getExpectedResult())
                .actualResult(report.getActualResult())
                .pageUrl(report.getPageUrl())
                .userAgent(report.getUserAgent())
                .appVersion(report.getAppVersion())
                .createdAt(report.getCreatedAt())
                .updatedAt(report.getUpdatedAt())
                .createdBy(toUserSummary(report.getCreatedByUser()))
                .assignedTo(toUserSummary(report.getAssignedToUser()))
                .build();
    }

    private ReportResponse.UserSummary toUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return ReportResponse.UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    private String toShortDescription(String description) {
        if (description == null) {
            return null;
        }

        int maxLength = 120;
        if (description.length() <= maxLength) {
            return description;
        }

        return description.substring(0, maxLength) + "...";
    }
}
