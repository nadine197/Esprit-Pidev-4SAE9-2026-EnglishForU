package tn.spring.discussion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.discussion.models.DiscussionComment;
import tn.spring.discussion.models.DiscussionPost;

import java.util.List;

public interface DiscussionCommentRepository extends JpaRepository<DiscussionComment, Long> {
    List<DiscussionComment> findByPostOrderByCreatedAtAsc(DiscussionPost post);

    long countByPost(DiscussionPost post);
}
