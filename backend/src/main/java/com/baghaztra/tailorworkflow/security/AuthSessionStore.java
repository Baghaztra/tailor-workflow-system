package com.baghaztra.tailorworkflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthSessionStore {

    private record Session(String refreshHash, Instant expiresAt) {
    }

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();
    private final long refreshTtlSeconds;

    public AuthSessionStore(@Value("${app.jwt.refresh-ttl-hours:8}") long refreshTtlHours) {
        this.refreshTtlSeconds = refreshTtlHours * 3600;
    }

    public IssuedSession create() {
        cleanupExpired();
        String sessionId = randomToken();
        String secret = randomToken();
        sessions.put(
                sessionId,
                new Session(hash(secret), Instant.now().plusSeconds(refreshTtlSeconds)));
        return new IssuedSession(sessionId, sessionId + "." + secret);
    }

    public boolean isActive(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            return false;
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId, session);
            return false;
        }
        return true;
    }

    public Optional<String> validateRefresh(String token) {
        int separator = token.indexOf('.');
        if (separator <= 0 || separator == token.length() - 1) {
            return Optional.empty();
        }

        String sessionId = token.substring(0, separator);
        String secret = token.substring(separator + 1);
        Session session = sessions.get(sessionId);
        if (session == null) {
            return Optional.empty();
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId, session);
            return Optional.empty();
        }

        byte[] expected = session.refreshHash().getBytes(StandardCharsets.UTF_8);
        byte[] actual = hash(secret).getBytes(StandardCharsets.UTF_8);
        if (!MessageDigest.isEqual(expected, actual)) {
            return Optional.empty();
        }
        return Optional.of(sessionId);
    }

    public void revokeByRefreshToken(String token) {
        int separator = token.indexOf('.');
        if (separator > 0) {
            sessions.remove(token.substring(0, separator));
        }
    }

    public long refreshTtlSeconds() {
        return refreshTtlSeconds;
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        sessions.entrySet().removeIf(entry -> entry.getValue().expiresAt().isBefore(now));
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
