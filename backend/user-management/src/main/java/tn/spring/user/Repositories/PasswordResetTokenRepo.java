package tn.spring.user.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import tn.spring.user.Models.PasswordResetToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface PasswordResetTokenRepo extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenSha256(String tokenSha256);
    @Transactional
    @Modifying
    @Query("update PasswordResetToken t set t.revokedAt = :now " +
            "where t.userId = :userId and t.usedAt is null and t.revokedAt is null")
    int revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("now") Instant now);
}

