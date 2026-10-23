package com.baghaztra.tailorworkflow.security;

import org.springframework.http.ResponseCookie;

import java.time.Duration;

public final class AuthCookie {

    public static final String NAME = "tailor_token";

    private AuthCookie() {
    }

    public static ResponseCookie create(String token, boolean secure, long ttlSeconds) {
        return base(token, secure)
                .maxAge(Duration.ofSeconds(ttlSeconds))
                .build();
    }

    public static ResponseCookie clear(boolean secure) {
        return base("", secure)
                .maxAge(Duration.ZERO)
                .build();
    }

    private static ResponseCookie.ResponseCookieBuilder base(String value, boolean secure) {
        return ResponseCookie.from(NAME, value)
                .httpOnly(true)
                .secure(secure)
                .path("/")
                .sameSite("Lax");
    }
}
