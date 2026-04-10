package tn.spring.user.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import tn.spring.user.Enums.NotificationType;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConstraintInitializer {

    private static final String CONSTRAINT_NAME = "notifications_type_check";
    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureNotificationTypeConstraint() {
        String allowedTypes = Arrays.stream(NotificationType.values())
                .map(NotificationType::name)
                .map(type -> "'" + type + "'")
                .collect(Collectors.joining(","));

        String dropConstraintSql = "ALTER TABLE IF EXISTS notifications DROP CONSTRAINT IF EXISTS " + CONSTRAINT_NAME;
        String addConstraintSql = "ALTER TABLE IF EXISTS notifications ADD CONSTRAINT " + CONSTRAINT_NAME
                + " CHECK (type IN (" + allowedTypes + "))";

        try {
            jdbcTemplate.execute(dropConstraintSql);
            jdbcTemplate.execute(addConstraintSql);
            log.info("Ensured notification type constraint '{}' with values: {}", CONSTRAINT_NAME, allowedTypes);
        } catch (DataAccessException ex) {
            log.warn("Unable to ensure notification type constraint '{}': {}", CONSTRAINT_NAME, ex.getMessage());
        }
    }
}
