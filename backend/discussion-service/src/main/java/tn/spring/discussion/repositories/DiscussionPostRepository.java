package tn.spring.discussion.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.spring.discussion.models.DiscussionPost;

import java.util.List;

public interface DiscussionPostRepository extends JpaRepository<DiscussionPost, Long> {
    List<DiscussionPost> findAllByOrderByCreatedAtDesc();
}
