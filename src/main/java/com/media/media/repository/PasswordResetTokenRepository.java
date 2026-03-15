package com.media.media.repository;

import com.media.media.model.PasswordResetToken;
import com.media.media.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    void deleteByUserAndUsedAtIsNull(User user);

    Optional<PasswordResetToken> findByTokenHashAndUsedAtIsNullAndExpiresAtGreaterThan(String tokenHash, Long now);
}
