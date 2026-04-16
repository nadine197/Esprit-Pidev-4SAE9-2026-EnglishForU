package tn.spring.discussion.models;

import jakarta.persistence.*;
import lombok.*;
import tn.spring.discussion.enums.DiscussionReactionType;

import java.time.Instant;

@Entity
@Table(
        name = "discussion_reactions",
        uniqueConstraints = @UniqueConstraint(name = "uk_discussion_post_author", columnNames = {"post_id", "author_email"})
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionReaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private DiscussionPost post;

    @Column(name = "author_email", nullable = false, length = 255)
    private String authorEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DiscussionReactionType type;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    public void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
