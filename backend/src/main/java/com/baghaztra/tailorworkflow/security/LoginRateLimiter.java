package com.baghaztra.tailorworkflow.security;

import com.baghaztra.tailorworkflow.entity.LoginAttempt;
import com.baghaztra.tailorworkflow.repository.LoginAttemptRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class LoginRateLimiter {

    private final LoginAttemptRepository attempts;
    private final int maxAttempts;
    private final Duration window;

    public LoginRateLimiter(
            LoginAttemptRepository attempts,
            @Value("${app.auth.max-attempts:5}") int maxAttempts,
            @Value("${app.auth.window-minutes:15}") long windowMinutes) {
        this.attempts = attempts;
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    @Transactional
    public Optional<Long> retryAfterSeconds(String key) {
        Instant now = Instant.now();
        Instant cutoff = now.minus(window);
        attempts.deleteByAttemptedAtBefore(cutoff);

        if (attempts.countByAttemptKeyAndAttemptedAtAfter(key, cutoff) < maxAttempts) {
            return Optional.empty();
        }

        Instant first = attempts
                .findFirstByAttemptKeyAndAttemptedAtAfterOrderByAttemptedAtAsc(key, cutoff)
                .map(LoginAttempt::getAttemptedAt)
                .orElse(now);
        long seconds = Duration.between(now, first.plus(window)).getSeconds();
        return Optional.of(Math.max(seconds, 1));
    }

    @Transactional
    public void recordFailure(String key) {
        attempts.save(new LoginAttempt(key, Instant.now()));
    }

    @Transactional
    public void clear(String key) {
        attempts.deleteByAttemptKey(key);
    }
}
