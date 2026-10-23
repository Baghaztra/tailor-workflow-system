package com.baghaztra.tailorworkflow.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class AuthCookie {

    public static final String ACCESS_NAME = "tailor_token";
    public static final String REFRESH_NAME = "tailor_refresh";

    private AuthCookie() {
    }

    public static ResponseCookie access(String token, boolean secure, long ttlSeconds) {
        return base(ACCESS_NAME, token, secure, "/")
                .maxAge(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    public static ResponseCookie refresh(String token, boolean secure, long ttlSeconds) {
        return base(REFRESH_NAME, token, secure, "/api/auth")
                .maxAge(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    public static ResponseCookie clearAccess(boolean secure) {
        return base(ACCESS_NAME, "", secure, "/")
                .maxAge(Duration.ZERO)
                .build();
    }

    public static ResponseCookie clearRefresh(boolean secure) {
        return base(REFRESH_NAME, "", secure, "/api/auth")
                .maxAge(Duration.ZERO)
                .build();
    }

    private static ResponseCookie.ResponseCookieBuilder base(
            String name,
            String value,
            boolean secure,
            String path) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .path(path)
                .sameSite("Lax");
    }
}
