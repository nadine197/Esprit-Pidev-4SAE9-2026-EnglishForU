package tn.spring.user.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.CreateReportCommentRequest;
import tn.spring.user.DTOs.CreateReportRequest;
import tn.spring.user.DTOs.NotificationPublishRequest;
import tn.spring.user.DTOs.ReportActivityResponse;
import tn.spring.user.DTOs.ReportCommentResponse;
import tn.spring.user.DTOs.ReportResponse;
import tn.spring.user.DTOs.UpdateReportRequest;
import tn.spring.user.Enums.NotificationType;
import tn.spring.user.Enums.ReportActivityType;
import tn.spring.user.Enums.ReportStatus;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.ReportActivity;
import tn.spring.user.Models.ReportComment;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.ReportActivityRepo;
import tn.spring.user.Repositories.ReportCommentRepo;
import tn.spring.user.Repositories.ReportRepo;
import tn.spring.user.Repositories.UserRepos;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {

    private static final Map<ReportStatus, Set<ReportStatus>> ALLOWED_TRANSITIONS = Map.of(
            ReportStatus.NEW, Set.of(ReportStatus.TRIAGED, ReportStatus.IN_PROGRESS, ReportStatus.CLOSED),
            ReportStatus.TRIAGED, Set.of(ReportStatus.IN_PROGRESS, ReportStatus.DONE, ReportStatus.CLOSED),
            ReportStatus.IN_PROGRESS, Set.of(ReportStatus.DONE, ReportStatus.CLOSED, ReportStatus.TRIAGED),
            ReportStatus.DONE, Set.of(ReportStatus.CLOSED, ReportStatus.IN_PROGRESS),
            ReportStatus.CLOSED, Set.of(ReportStatus.TRIAGED)
    );

    private final ReportRepo reportRepo;
    private final ReportCommentRepo reportCommentRepo;
    private final ReportActivityRepo reportActivityRepo;
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
            createActivity(savedReport, reporter, ReportActivityType.REPORT_CREATED, null, ReportStatus.NEW,
                "Report submitted by " + fullName(reporter));
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
    public ReportResponse getReportDetails(Long reportId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Report report = findReportById(reportId);
        ensureCanAccessReport(report, currentUser);
        return toReportResponse(report);
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

    @Transactional(readOnly = true)
    public List<ReportCommentResponse> listReportComments(Long reportId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Report report = findReportById(reportId);
        ensureCanAccessReport(report, currentUser);

        return reportCommentRepo.findByReportOrderByCreatedAtAsc(report)
                .stream()
                .map(this::toReportCommentResponse)
                .toList();
    }

    public ReportCommentResponse addReportComment(Long reportId,
                                                  CreateReportCommentRequest request,
                                                  String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Report report = findReportById(reportId);
        ensureCanAccessReport(report, currentUser);

        String commentMessage = nullableTrim(request.getMessage());
        if (commentMessage == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "COMMENT_MESSAGE_REQUIRED");
        }

        ReportComment comment = createComment(report, currentUser, commentMessage);
        createActivity(report, currentUser, ReportActivityType.COMMENT_ADDED, null, null,
                "Comment added by " + fullName(currentUser));
        createCommentNotifications(report, currentUser, commentMessage);

        return toReportCommentResponse(comment);
    }

    @Transactional(readOnly = true)
    public List<ReportActivityResponse> listReportActivity(Long reportId, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        Report report = findReportById(reportId);
        ensureCanAccessReport(report, currentUser);

        return reportActivityRepo.findByReportOrderByCreatedAtDesc(report)
                .stream()
                .map(this::toReportActivityResponse)
                .toList();
    }

    public ReportResponse updateReport(Long reportId, UpdateReportRequest request, String currentUserEmail) {
        User currentUser = findUserByEmail(currentUserEmail);
        ensureIsStaff(currentUser);

        Report report = findReportById(reportId);

        ReportStatus previousStatus = report.getStatus();
        User previousAssignee = report.getAssignedToUser();

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
            validateStatusTransition(report.getStatus(), request.getStatus());
            report.setStatus(request.getStatus());
        }

        if (Boolean.TRUE.equals(request.getUnassign())) {
            report.setAssignedToUser(null);
        } else if (Boolean.TRUE.equals(request.getAssignToMe())) {
            report.setAssignedToUser(currentUser);
        } else if (request.getAssignedToUserId() != null) {
            User assignee = findUserById(request.getAssignedToUserId());
            report.setAssignedToUser(assignee);
        }

        Report updated = reportRepo.save(report);

        if (previousStatus != updated.getStatus()) {
            createActivity(updated, currentUser, ReportActivityType.STATUS_CHANGED, previousStatus, updated.getStatus(),
                    "Status changed from " + previousStatus + " to " + updated.getStatus());
            createStatusChangeNotifications(updated, currentUser, previousStatus, updated.getStatus());
        }

        if (!sameUser(previousAssignee, updated.getAssignedToUser())) {
            if (updated.getAssignedToUser() == null) {
                createActivity(updated, currentUser, ReportActivityType.UNASSIGNED, null, null,
                        "Report unassigned by " + fullName(currentUser));
            } else {
                createActivity(updated, currentUser, ReportActivityType.ASSIGNED, null, null,
                        "Report assigned to " + fullName(updated.getAssignedToUser()));
                createAssignmentNotifications(updated, currentUser);
            }
        }

        String requestInfoMessage = nullableTrim(request.getRequestInfoMessage());
        if (requestInfoMessage != null) {
            createComment(updated, currentUser, requestInfoMessage);
            createActivity(updated, currentUser, ReportActivityType.REQUEST_INFO, null, null,
                    "Requested additional information");
            createRequestInfoNotifications(updated, currentUser, requestInfoMessage);
        }

        return toReportResponse(updated);
    }

    private Report findReportById(Long reportId) {
        return reportRepo.findById(reportId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "REPORT_NOT_FOUND"));
    }

    private ReportComment createComment(Report report, User author, String message) {
        ReportComment comment = ReportComment.builder()
                .report(report)
                .authorUser(author)
                .message(message)
                .build();
        return reportCommentRepo.save(comment);
    }

    private void ensureCanAccessReport(Report report, User currentUser) {
        if (isStaffRole(currentUser.getRole())) {
            return;
        }

        if (sameUser(report.getCreatedByUser(), currentUser) || sameUser(report.getAssignedToUser(), currentUser)) {
            return;
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "REPORT_ACCESS_FORBIDDEN");
    }

    private void ensureIsStaff(User currentUser) {
        if (!isStaffRole(currentUser.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "REPORT_UPDATE_FORBIDDEN");
        }
    }

    private boolean isStaffRole(UserRole role) {
        return role == UserRole.HELP_DESK || role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }

    private boolean sameUser(User a, User b) {
        if (a == null || b == null || a.getId() == null || b.getId() == null) {
            return false;
        }
        return a.getId().equals(b.getId());
    }

    private void validateStatusTransition(ReportStatus currentStatus, ReportStatus targetStatus) {
        if (currentStatus == targetStatus) {
            return;
        }

        Set<ReportStatus> allowedTargets = ALLOWED_TRANSITIONS.getOrDefault(currentStatus, Set.of());
        if (!allowedTargets.contains(targetStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "INVALID_REPORT_TRANSITION");
        }
    }

    private void createActivity(Report report,
                                User actor,
                                ReportActivityType type,
                                ReportStatus fromStatus,
                                ReportStatus toStatus,
                                String details) {
        ReportActivity activity = ReportActivity.builder()
                .report(report)
                .actorUser(actor)
                .type(type)
                .fromStatus(fromStatus)
                .toStatus(toStatus)
                .details(nullableTrim(details))
                .build();

        reportActivityRepo.save(activity);
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

    private void createStatusChangeNotifications(Report report,
                                                 User actor,
                                                 ReportStatus previousStatus,
                                                 ReportStatus newStatus) {
        List<User> recipients = collectUniqueRecipients(report.getCreatedByUser(), report.getAssignedToUser());
        recipients.removeIf(candidate -> sameUser(candidate, actor));
        if (recipients.isEmpty()) {
            return;
        }

        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_STATUS_CHANGED)
                .title("Report status updated")
                .message("" + fullName(actor) + " changed report #" + report.getId() + " from " + previousStatus + " to " + newStatus)
                .link("/helpdesk/board?ticketId=" + report.getId())
                .reportId(report.getId())
                .build();

        notificationPublisher.publishToUsers(recipients, request);
    }

    private void createAssignmentNotifications(Report report, User actor) {
        User assignee = report.getAssignedToUser();
        if (assignee == null || sameUser(assignee, actor)) {
            return;
        }

        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_ASSIGNED)
                .title("Report assigned")
                .message("" + fullName(actor) + " assigned you to report #" + report.getId() + ": " + report.getTitle())
                .link("/helpdesk/board?ticketId=" + report.getId())
                .reportId(report.getId())
                .build();

        notificationPublisher.publishToUser(assignee.getId(), request);
    }

    private void createCommentNotifications(Report report, User commentAuthor, String message) {
        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_COMMENT_ADDED)
                .title("New report comment")
                .message("" + fullName(commentAuthor) + " commented on report #" + report.getId() + ": " + toShortDescription(message))
                .link("/helpdesk/board?ticketId=" + report.getId())
                .reportId(report.getId())
                .build();

        if (isStaffRole(commentAuthor.getRole())) {
            if (!sameUser(report.getCreatedByUser(), commentAuthor)) {
                notificationPublisher.publishToUser(report.getCreatedByUser().getId(), request);
            }
            return;
        }

        List<User> recipients = new ArrayList<>(userRepos.findByRole(UserRole.HELP_DESK));
        if (report.getAssignedToUser() != null) {
            recipients.add(report.getAssignedToUser());
        }

        List<User> deduplicatedRecipients = deduplicateUsers(recipients);
        deduplicatedRecipients.removeIf(candidate -> sameUser(candidate, commentAuthor));
        if (deduplicatedRecipients.isEmpty()) {
            return;
        }

        notificationPublisher.publishToUsers(deduplicatedRecipients, request);
    }

    private void createRequestInfoNotifications(Report report, User actor, String requestMessage) {
        if (sameUser(report.getCreatedByUser(), actor)) {
            return;
        }

        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_COMMENT_ADDED)
                .title("Helpdesk requested more information")
                .message("" + fullName(actor) + " requested more details on report #" + report.getId() + ": " + toShortDescription(requestMessage))
                .link("/user/main?ticketId=" + report.getId())
                .reportId(report.getId())
                .build();

        notificationPublisher.publishToUser(report.getCreatedByUser().getId(), request);
    }

    private List<User> collectUniqueRecipients(User... users) {
        List<User> recipientList = new ArrayList<>();
        for (User user : users) {
            if (user != null) {
                recipientList.add(user);
            }
        }
        return deduplicateUsers(recipientList);
    }

    private List<User> deduplicateUsers(List<User> users) {
        LinkedHashSet<UUID> seenIds = new LinkedHashSet<>();
        List<User> deduplicated = new ArrayList<>();

        for (User user : users) {
            if (user == null || user.getId() == null) {
                continue;
            }
            if (seenIds.add(user.getId())) {
                deduplicated.add(user);
            }
        }

        return deduplicated;
    }

    private String fullName(User user) {
        if (user == null) {
            return "System";
        }
        String firstName = user.getName() == null ? "" : user.getName().trim();
        String lastName = user.getLastName() == null ? "" : user.getLastName().trim();
        String combined = (firstName + " " + lastName).trim();
        return combined.isEmpty() ? user.getEmail() : combined;
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

    private ReportCommentResponse toReportCommentResponse(ReportComment comment) {
        return ReportCommentResponse.builder()
                .id(comment.getId())
                .reportId(comment.getReport().getId())
                .message(comment.getMessage())
                .createdAt(comment.getCreatedAt())
                .author(toCommentUserSummary(comment.getAuthorUser()))
                .build();
    }

    private ReportActivityResponse toReportActivityResponse(ReportActivity activity) {
        return ReportActivityResponse.builder()
                .id(activity.getId())
                .reportId(activity.getReport().getId())
                .type(activity.getType())
                .fromStatus(activity.getFromStatus())
                .toStatus(activity.getToStatus())
                .details(activity.getDetails())
                .createdAt(activity.getCreatedAt())
                .actor(toActivityUserSummary(activity.getActorUser()))
                .build();
    }

    private ReportCommentResponse.UserSummary toCommentUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return ReportCommentResponse.UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }

    private ReportActivityResponse.UserSummary toActivityUserSummary(User user) {
        if (user == null) {
            return null;
        }

        return ReportActivityResponse.UserSummary.builder()
                .id(user.getId())
                .name(user.getName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
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
