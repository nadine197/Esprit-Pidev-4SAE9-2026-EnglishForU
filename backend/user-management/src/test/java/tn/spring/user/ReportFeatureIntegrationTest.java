package tn.spring.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.user.Enums.ReportCategory;
import tn.spring.user.Enums.ReportSeverity;
import tn.spring.user.Enums.ReportStatus;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Report;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.NotificationRepo;
import tn.spring.user.Repositories.ReportRepo;
import tn.spring.user.Repositories.UserRepos;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReportFeatureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepos userRepos;

    @Autowired
    private ReportRepo reportRepo;

    @Autowired
    private NotificationRepo notificationRepo;

    private User reporter;
    private User helpDeskA;
    private User helpDeskB;

    @BeforeEach
    void setup() {
        notificationRepo.deleteAll();
        reportRepo.deleteAll();
        userRepos.deleteAll();

        reporter = userRepos.save(User.builder()
                .name("Student")
                .lastName("Reporter")
                .email("student@englishforu.local")
                .password("encoded")
                .active(true)
                .role(UserRole.STUDENT)
                .build());

        helpDeskA = userRepos.save(User.builder()
                .name("Help")
                .lastName("DeskA")
                .email("helpdesk1@englishforu.local")
                .password("encoded")
                .active(true)
                .role(UserRole.HELP_DESK)
                .build());

        helpDeskB = userRepos.save(User.builder()
                .name("Help")
                .lastName("DeskB")
                .email("helpdesk2@englishforu.local")
                .password("encoded")
                .active(true)
                .role(UserRole.HELP_DESK)
                .build());
    }

    @Test
    void creatingReportPersistsRowAndCreatesNotificationsForHelpDeskUsers() throws Exception {
        Map<String, Object> payload = Map.of(
                "title", "Cannot submit quiz",
                "category", "BUG",
                "severity", "HIGH",
                "description", "Submit button is disabled after answering all questions.",
                "stepsToReproduce", "1. Open quiz. 2. Answer. 3. Click submit.",
                "expectedResult", "Quiz should submit",
                "actualResult", "Button remains disabled",
                "pageUrl", "/courses/quiz/45",
                "userAgent", "Mozilla/Test",
                "appVersion", "1.0.0"
        );

        mockMvc.perform(post("/api/reports")
                        .with(user(reporter.getEmail()).authorities(new SimpleGrantedAuthority("STUDENT")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Cannot submit quiz"))
                .andExpect(jsonPath("$.status").value("NEW"))
                .andExpect(jsonPath("$.createdBy.email").value(reporter.getEmail()));

        assertThat(reportRepo.findAll()).hasSize(1);
        assertThat(notificationRepo.findByRecipientUserOrderByCreatedAtDesc(helpDeskA)).hasSize(1);
        assertThat(notificationRepo.findByRecipientUserOrderByCreatedAtDesc(helpDeskB)).hasSize(1);
    }

    @Test
    void nonHelpDeskUserCannotAccessHelpDeskEndpoints() throws Exception {
        mockMvc.perform(get("/api/helpdesk/reports")
                        .with(user(reporter.getEmail()).authorities(new SimpleGrantedAuthority("STUDENT"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void helpDeskUserCanUpdateTicketStatus() throws Exception {
        Report report = reportRepo.save(Report.builder()
                .title("API timeout")
                .category(ReportCategory.ISSUE)
                .severity(ReportSeverity.MEDIUM)
                .description("API request times out after 30 seconds")
                .status(ReportStatus.NEW)
                .createdByUser(reporter)
                .build());

        Map<String, Object> patchPayload = Map.of("status", "IN_PROGRESS");

        mockMvc.perform(patch("/api/helpdesk/reports/{id}", report.getId())
                        .with(user(helpDeskA.getEmail()).authorities(new SimpleGrantedAuthority("HELP_DESK")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        Report updated = reportRepo.findById(report.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(ReportStatus.IN_PROGRESS);
    }
}
