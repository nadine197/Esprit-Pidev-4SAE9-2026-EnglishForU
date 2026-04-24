package tn.spring.discussion.models;

import jakarta.persistence.*;
import lombok.*;
import tn.spring.discussion.enums.DiscussionPostType;

import java.time.Instant;

@Entity
@Table(name = "discussion_posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscussionPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String courseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DiscussionPostType type;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(length = 500)
    private String imagePath;

    @Column(columnDefinition = "TEXT")
    private String quizPayload;

    @Column(nullable = false, length = 255)
    private String authorEmail;

    @Column(nullable = false, length = 40)
    private String authorRole;

    @Column(length = 20)
    private String authorLevel;

    @Column(length = 40)
    private String targetRole;

    @Column(length = 20)
    private String targetLevel;

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
