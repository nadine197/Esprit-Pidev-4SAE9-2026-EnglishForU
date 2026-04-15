package tn.spring.discussion.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import tn.spring.discussion.enums.DiscussionReactionType;

@Data
public class CreateDiscussionReactionRequest {

    @NotNull(message = "REACTION_TYPE_REQUIRED")
    private DiscussionReactionType type;
}
