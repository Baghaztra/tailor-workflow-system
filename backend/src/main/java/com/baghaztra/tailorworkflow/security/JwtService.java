package com.baghaztra.tailorworkflow.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final long ttlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.ttl-hours:8}") long ttlHours) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
        this.ttlSeconds = ttlHours * 3600;
    }

    public String create(String subject) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(subject)
                .withClaim("role", "ADMIN")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(ttlSeconds)))
                .sign(algorithm);
    }

    public Optional<String> verify(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            return Optional.ofNullable(decoded.getSubject());
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public long ttlSeconds() {
        return ttlSeconds;
    }
}
