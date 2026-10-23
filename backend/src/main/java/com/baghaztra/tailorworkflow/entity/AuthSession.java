package com.baghaztra.tailorworkflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "auth_sessions")
public class AuthSession {

    @Id
    @Column(length = 64, nullable = false, updatable = false)
    private String id;

    @Column(name = "refresh_hash", nullable = false, length = 64, updatable = false)
    private String refreshHash;

    @Column(name = "expires_at", nullable = false, updatable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected AuthSession() {
    }

    public AuthSession(String id, String refreshHash, Instant expiresAt) {
        this.id = id;
        this.refreshHash = refreshHash;
        this.expiresAt = expiresAt;
    }

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public String getRefreshHash() {
        return refreshHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }
}
