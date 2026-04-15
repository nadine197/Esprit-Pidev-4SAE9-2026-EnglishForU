package tn.spring.discussion.dtos;

import lombok.Builder;
import lombok.Data;
import tn.spring.discussion.enums.DiscussionReactionType;

import java.time.Instant;

@Data
@Builder
public class DiscussionReactionResponse {
    private Long id;
    private Long postId;
    private String authorEmail;
    private DiscussionReactionType type;
    private Instant createdAt;
}
