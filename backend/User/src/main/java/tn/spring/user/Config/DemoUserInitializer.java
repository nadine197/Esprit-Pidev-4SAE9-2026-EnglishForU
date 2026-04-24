package tn.spring.user.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.user.Enums.UserRole;
import tn.spring.user.Models.Student;
import tn.spring.user.Models.User;
import tn.spring.user.Repositories.UserRepos;
import tn.spring.user.Services.UserService;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DemoUserInitializer implements CommandLineRunner {

    private static final String DEFAULT_PREFIX = "+216";

    private final UserRepos userRepos;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.demo.seed.enabled:true}")
    private boolean demoSeedEnabled;

    @Value("${app.demo.student.email}")
    private String studentEmail;

    @Value("${app.demo.student.password}")
    private String studentPassword;

    @Value("${app.demo.admin.email}")
    private String adminEmail;

    @Value("${app.demo.admin.password}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        if (!demoSeedEnabled) {
            return;
        }

        ensureStudentAccount();
        ensureAdminAccount();
    }

    private void ensureStudentAccount() {
        String normalizedEmail = normalizeEmail(studentEmail);

        userRepos.findByEmailIgnoreCase(normalizedEmail)
                .ifPresentOrElse(
                        existing -> refreshExistingUser(existing, studentPassword, UserRole.STUDENT),
                        this::createStudentAccount
                );
    }

    private void ensureAdminAccount() {
        String normalizedEmail = normalizeEmail(adminEmail);

        userRepos.findByEmailIgnoreCase(normalizedEmail)
                .ifPresentOrElse(
                        existing -> refreshExistingUser(existing, adminPassword, UserRole.ADMIN),
                        this::createAdminAccount
                );
    }

    private void createStudentAccount() {
        User student = Student.builder()
                .name("Demo")
                .lastName("Student")
                .email(normalizeEmail(studentEmail))
                .password(studentPassword)
                .prefix(DEFAULT_PREFIX)
                .phone("2026041201")
                .role(UserRole.STUDENT)
                .active(true)
                .englishLevel("A2")
                .learningGoal("Practice")
                .dailyGoalMinutes(30)
                .build();

        userService.createStudentOrTutor(student);
    }

    private void createAdminAccount() {
        User admin = User.builder()
                .name("Demo")
                .lastName("Admin")
                .email(normalizeEmail(adminEmail))
                .password(adminPassword)
                .prefix(DEFAULT_PREFIX)
                .phone("2026041202")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        userService.createUser(admin);
    }

    private void refreshExistingUser(User user, String rawPassword, UserRole role) {
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setActive(true);
        user.setRole(role);

        if (user.getPrefix() == null || user.getPrefix().isBlank()) {
            user.setPrefix(DEFAULT_PREFIX);
        }

        userRepos.save(user);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
