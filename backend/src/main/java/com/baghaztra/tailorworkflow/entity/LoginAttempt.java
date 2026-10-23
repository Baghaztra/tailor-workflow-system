package com.baghaztra.tailorworkflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "login_attempts",
        indexes = @Index(
                name = "idx_login_attempts_key_time",
                columnList = "attempt_key, attempted_at"))
public class LoginAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_key", nullable = false, length = 255)
    private String attemptKey;

    @Column(name = "attempted_at", nullable = false, updatable = false)
    private Instant attemptedAt;

    protected LoginAttempt() {
    }

    public LoginAttempt(String attemptKey, Instant attemptedAt) {
        this.attemptKey = attemptKey;
        this.attemptedAt = attemptedAt;
    }

    public String getAttemptKey() {
        return attemptKey;
    }

    public Instant getAttemptedAt() {
        return attemptedAt;
    }
}
