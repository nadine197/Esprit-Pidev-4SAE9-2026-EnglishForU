package tn.spring.discussion.dtos;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class DiscussionCommentResponse {
    private Long id;
    private Long postId;
    private String authorEmail;
    private String message;
    private Instant createdAt;
}
