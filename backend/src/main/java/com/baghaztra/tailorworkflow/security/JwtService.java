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
    private final long accessTtlSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-ttl-minutes:15}") long accessTtlMinutes) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters");
        }
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).build();
        this.accessTtlSeconds = accessTtlMinutes * 60;
    }

    public String createAccess(String username, String sessionId) {
        Instant now = Instant.now();
        return JWT.create()
                .withSubject(username)
                .withClaim("role", "ADMIN")
                .withClaim("type", "access")
                .withClaim("sid", sessionId)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plusSeconds(accessTtlSeconds)))
                .sign(algorithm);
    }

    public Optional<VerifiedAccess> verifyAccess(String token) {
        try {
            DecodedJWT decoded = verifier.verify(token);
            String username = decoded.getSubject();
            String sessionId = decoded.getClaim("sid").asString();
            boolean isAccess = "access".equals(decoded.getClaim("type").asString());

            if (username == null || sessionId == null || sessionId.isBlank() || !isAccess) {
                return Optional.empty();
            }
            return Optional.of(new VerifiedAccess(username, sessionId));
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public long accessTtlSeconds() {
        return accessTtlSeconds;
    }

    public record VerifiedAccess(String username, String sessionId) {
    }
}
