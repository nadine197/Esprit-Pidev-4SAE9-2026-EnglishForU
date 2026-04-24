package tn.spring.user.Models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter

@ToString
@Entity
@Table(name = "password_reset_tokens",
        indexes = {
                @Index(name="idx_prt_lookup", columnList = "tokenSha256"),
                @Index(name="idx_prt_user", columnList = "userId")
        })
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false, length = 64) // hex sha256
    private String tokenSha256;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

    private Instant usedAt;
    private Instant revokedAt;

    // getters/setters...
    public boolean isActive() {
        return usedAt == null && revokedAt == null  && Instant.now().isBefore(expiresAt);
    }
}
