package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.security.AdminCredentials;
import com.baghaztra.tailorworkflow.security.AuthCookie;
import com.baghaztra.tailorworkflow.security.AuthSessionStore;
import com.baghaztra.tailorworkflow.security.JwtService;
import com.baghaztra.tailorworkflow.security.LoginRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminCredentials admin;
    private final JwtService jwt;
    private final AuthSessionStore sessions;
    private final LoginRateLimiter rateLimiter;
    private final boolean secureCookie;

    public AuthController(
            AdminCredentials admin,
            JwtService jwt,
            AuthSessionStore sessions,
            LoginRateLimiter rateLimiter,
            @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.admin = admin;
        this.jwt = jwt;
        this.sessions = sessions;
        this.rateLimiter = rateLimiter;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String key = rateKey(httpRequest, request.username());
        Optional<Long> retryAfter = rateLimiter.retryAfterSeconds(key);
        if (retryAfter.isPresent()) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .header(HttpHeaders.RETRY_AFTER, String.valueOf(retryAfter.get()))
                    .body(new ErrorResponse("Too many login attempts. Try again later."));
        }

        if (!admin.matches(request.username(), request.password())) {
            rateLimiter.recordFailure(key);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials"));
        }
        rateLimiter.clear(key);

        AuthSessionStore.IssuedSession session = sessions.create();
        String accessToken = jwt.createAccess(admin.username(), session.sessionId());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        AuthCookie.access(accessToken, secureCookie, jwt.accessTtlSeconds()).toString(),
                        AuthCookie.refresh(session.refreshToken(), secureCookie, sessions.refreshTtlSeconds())
                                .toString())
                .body(new AdminResponse(admin.username()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = AuthCookie.REFRESH_NAME, required = false) String refreshToken) {
        if (refreshToken == null) {
            return unauthorized();
        }

        Optional<String> sessionId = sessions.validateRefresh(refreshToken);
        if (sessionId.isEmpty()) {
            return unauthorized();
        }

        String accessToken = jwt.createAccess(admin.username(), sessionId.get());
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        AuthCookie.access(accessToken, secureCookie, jwt.accessTtlSeconds()).toString())
                .body(new AdminResponse(admin.username()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = AuthCookie.REFRESH_NAME, required = false) String refreshToken) {
        if (refreshToken != null) {
            sessions.revokeByRefreshToken(refreshToken);
        }

        return ResponseEntity.noContent()
                .header(
                        HttpHeaders.SET_COOKIE,
                        AuthCookie.clearAccess(secureCookie).toString(),
                        AuthCookie.clearRefresh(secureCookie).toString())
                .build();
    }

    @GetMapping("/me")
    public AdminResponse me(Authentication authentication) {
        return new AdminResponse(authentication.getName());
    }

    private String rateKey(HttpServletRequest request, String username) {
        return request.getRemoteAddr() + "|" + username.toLowerCase(Locale.ROOT);
    }

    private ResponseEntity<ErrorResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("Session expired"));
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record AdminResponse(String username) {
    }

    public record ErrorResponse(String message) {
    }
}
