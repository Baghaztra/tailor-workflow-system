package com.baghaztra.tailorworkflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class LoginRateLimiter {

    private final int maxAttempts;
    private final Duration window;
    private final Map<String, Deque<Instant>> failures = new HashMap<>();

    public LoginRateLimiter(
            @Value("${app.auth.max-attempts:5}") int maxAttempts,
            @Value("${app.auth.window-minutes:15}") long windowMinutes) {
        this.maxAttempts = maxAttempts;
        this.window = Duration.ofMinutes(windowMinutes);
    }

    public synchronized Optional<Long> retryAfterSeconds(String key) {
        Deque<Instant> attempts = failures.get(key);
        if (attempts == null) {
            return Optional.empty();
        }

        Instant cutoff = Instant.now().minus(window);
        while (!attempts.isEmpty() && attempts.peekFirst().isBefore(cutoff)) {
            attempts.pollFirst();
        }
        if (attempts.isEmpty()) {
            failures.remove(key);
            return Optional.empty();
        }
        if (attempts.size() < maxAttempts) {
            return Optional.empty();
        }

        long seconds = Duration.between(Instant.now(), attempts.peekFirst().plus(window)).getSeconds();
        return Optional.of(Math.max(seconds, 1));
    }

    public synchronized void recordFailure(String key) {
        failures.computeIfAbsent(key, ignored -> new ArrayDeque<>()).addLast(Instant.now());
    }

    public synchronized void clear(String key) {
        failures.remove(key);
    }
}
