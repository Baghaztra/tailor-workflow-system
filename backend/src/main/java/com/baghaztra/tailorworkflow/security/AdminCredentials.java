package com.baghaztra.tailorworkflow.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminCredentials {

    private final String username;
    private final String passwordHash;
    private final PasswordEncoder encoder;

    public AdminCredentials(
            @Value("${app.admin.username}") String username,
            @Value("${app.admin.password}") String password,
            PasswordEncoder encoder) {
        if (username == null || username.isBlank()) {
            throw new IllegalStateException("ADMIN_USERNAME is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalStateException("ADMIN_PASSWORD is required");
        }
        this.username = username;
        this.passwordHash = encoder.encode(password);
        this.encoder = encoder;
    }

    public boolean matches(String candidateUsername, String candidatePassword) {
        return username.equals(candidateUsername) && encoder.matches(candidatePassword, passwordHash);
    }

    public String username() {
        return username;
    }
}
