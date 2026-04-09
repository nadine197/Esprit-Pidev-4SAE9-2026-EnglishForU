package tn.spring.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.server.ResponseStatusException;
import tn.spring.user.DTOs.NotificationPublishRequest;
import tn.spring.user.DTOs.NotificationResponse;
import tn.spring.user.Enums.NotificationType;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Notification;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.NotificationRepo;
import tn.spring.user.Repositories.ReportRepo;
import tn.spring.user.Repositories.UserRepos;
import tn.spring.user.Services.NotificationService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepo notificationRepo;

    @Mock
    private UserRepos userRepos;

    @Mock
    private ReportRepo reportRepo;

        @Mock
        private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void publishToRoleCreatesOneNotificationPerRecipient() {
        User firstRecipient = buildUser("helpdesk1@englishforu.local");
        User secondRecipient = buildUser("helpdesk2@englishforu.local");

        when(userRepos.findByRole(UserRole.HELP_DESK)).thenReturn(List.of(firstRecipient, secondRecipient));
        when(notificationRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_CREATED)
                .title("New report submitted")
                .message("A report was submitted")
                .link("/helpdesk/board?ticketId=9")
                .build();

        long savedCount = notificationService.publishToRole(UserRole.HELP_DESK, request);

        assertThat(savedCount).isEqualTo(2);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepo).saveAll(captor.capture());

        List<Notification> savedNotifications = captor.getValue();
        assertThat(savedNotifications).hasSize(2);
        assertThat(savedNotifications)
                .extracting(Notification::getRecipientUser)
                .containsExactlyInAnyOrder(firstRecipient, secondRecipient);
        assertThat(savedNotifications)
                .extracting(Notification::getType)
                .containsOnly(NotificationType.REPORT_CREATED);

        verify(messagingTemplate, times(2)).convertAndSend(anyString(), org.mockito.ArgumentMatchers.any(NotificationResponse.class));
    }

    @Test
    void publishToUsersAttachesReportWhenReportIdIsProvided() {
        User recipient = buildUser("helpdesk@englishforu.local");
        Report report = Report.builder().id(42L).title("Report").build();

        when(reportRepo.findById(42L)).thenReturn(Optional.of(report));
        when(notificationRepo.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        NotificationPublishRequest request = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_STATUS_CHANGED)
                .title("Report updated")
                .message("The report status changed")
                .link("/helpdesk/board?ticketId=42")
                .reportId(42L)
                .build();

        long savedCount = notificationService.publishToUsers(List.of(recipient), request);

        assertThat(savedCount).isEqualTo(1);
        verify(reportRepo).findById(42L);

        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);
        verify(notificationRepo).saveAll(captor.capture());
                assertThat(captor.getValue().get(0).getReport()).isEqualTo(report);

                verify(messagingTemplate, times(2)).convertAndSend(anyString(), org.mockito.ArgumentMatchers.any(NotificationResponse.class));
    }

    @Test
    void publishToUsersRejectsMissingLink() {
        User recipient = buildUser("helpdesk@englishforu.local");

        NotificationPublishRequest invalidRequest = NotificationPublishRequest.builder()
                .type(NotificationType.REPORT_CREATED)
                .title("Invalid")
                .message("Invalid request")
                .link(" ")
                .build();

        assertThatThrownBy(() -> notificationService.publishToUsers(List.of(recipient), invalidRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                });
    }

    private User buildUser(String email) {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Help")
                .lastName("Desk")
                .email(email)
                .password("encoded")
                .active(true)
                .role(UserRole.HELP_DESK)
                .build();
    }
}
