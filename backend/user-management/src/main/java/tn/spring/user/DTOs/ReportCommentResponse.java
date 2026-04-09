package tn.spring.user.DTOs;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class ReportCommentResponse {
    private Long id;
    private Long reportId;
    private String message;
    private Instant createdAt;
    private UserSummary author;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String lastName;
        private String email;
    }
}
