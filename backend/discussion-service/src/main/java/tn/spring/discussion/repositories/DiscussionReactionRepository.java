package tn.spring.discussion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.discussion.models.DiscussionPost;
import tn.spring.discussion.models.DiscussionReaction;

import java.util.List;
import java.util.Optional;

public interface DiscussionReactionRepository extends JpaRepository<DiscussionReaction, Long> {
    Optional<DiscussionReaction> findByPostAndAuthorEmailIgnoreCase(DiscussionPost post, String authorEmail);

    List<DiscussionReaction> findByPost(DiscussionPost post);

    long countByPost(DiscussionPost post);
}
