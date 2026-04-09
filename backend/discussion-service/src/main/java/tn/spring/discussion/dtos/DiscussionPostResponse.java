package tn.spring.discussion.dtos;

import lombok.Builder;
import lombok.Data;
import tn.spring.discussion.enums.DiscussionPostType;
import tn.spring.discussion.enums.DiscussionReactionType;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class DiscussionPostResponse {
    private Long id;
    private String courseId;
    private DiscussionPostType type;
    private String content;
    private String imagePath;
    private String quizPayload;

    private String authorEmail;
    private String authorRole;
    private String authorLevel;

    private String targetRole;
    private String targetLevel;

    private Instant createdAt;
    private Instant updatedAt;

    private long commentCount;
    private long reactionCount;
    private DiscussionReactionType myReaction;

    private List<DiscussionCommentResponse> comments;
    private List<DiscussionReactionResponse> reactions;
}
