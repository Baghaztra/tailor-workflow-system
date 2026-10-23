package com.baghaztra.tailorworkflow.security;

import com.baghaztra.tailorworkflow.entity.AuthSession;
import com.baghaztra.tailorworkflow.repository.AuthSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

@Component
public class AuthSessionStore {

    private final AuthSessionRepository sessions;
    private final SecureRandom random = new SecureRandom();
    private final long refreshTtlSeconds;

    public AuthSessionStore(
            AuthSessionRepository sessions,
            @Value("${app.jwt.refresh-ttl-hours:8}") long refreshTtlHours) {
        this.sessions = sessions;
        this.refreshTtlSeconds = refreshTtlHours * 3600;
    }

    @Transactional
    public IssuedSession create() {
        sessions.deleteByExpiresAtBefore(Instant.now());
        String sessionId = randomToken();
        String secret = randomToken();
        sessions.save(new AuthSession(
                sessionId,
                hash(secret),
                Instant.now().plusSeconds(refreshTtlSeconds)));
        return new IssuedSession(sessionId, sessionId + "." + secret);
    }

    @Transactional(readOnly = true)
    public boolean isActive(String sessionId) {
        return sessions.findById(sessionId)
                .map(session -> session.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Transactional
    public Optional<String> validateRefresh(String token) {
        int separator = token.indexOf('.');
        if (separator <= 0 || separator == token.length() - 1) {
            return Optional.empty();
        }

        String sessionId = token.substring(0, separator);
        String secret = token.substring(separator + 1);
        Optional<AuthSession> found = sessions.findById(sessionId);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        AuthSession session = found.get();
        if (!session.getExpiresAt().isAfter(Instant.now())) {
            sessions.deleteById(sessionId);
            return Optional.empty();
        }

        byte[] expected = session.getRefreshHash().getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(secret).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            return Optional.empty();
        }
        return Optional.of(sessionId);
    }

    @Transactional
    public void revokeByRefreshToken(String token) {
        int separator = token.indexOf('.');
        if (separator > 0) {
            sessions.deleteById(token.substring(0, separator));
        }
    }

    public long refreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    public record IssuedSession(String sessionId, String refreshToken) {
    }
}
