package com.baghaztra.tailorworkflow.controller;

import com.baghaztra.tailorworkflow.security.AdminCredentials;
import com.baghaztra.tailorworkflow.security.AuthCookie;
import com.baghaztra.tailorworkflow.security.JwtService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminCredentials admin;
    private final JwtService jwt;
    private final boolean secureCookie;

    public AuthController(
            AdminCredentials admin,
            JwtService jwt,
            @Value("${app.cookie.secure:false}") boolean secureCookie) {
        this.admin = admin;
        this.jwt = jwt;
        this.secureCookie = secureCookie;
    }

    @PostMapping("/login")
    public ResponseEntity<AdminResponse> login(@Valid @RequestBody LoginRequest request) {
        if (!admin.matches(request.username(), request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwt.create(admin.username());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, AuthCookie.create(token, secureCookie, jwt.ttlSeconds()).toString())
                .body(new AdminResponse(admin.username()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, AuthCookie.clear(secureCookie).toString())
                .build();
    }

    @GetMapping("/me")
    public AdminResponse me(Authentication authentication) {
        return new AdminResponse(authentication.getName());
    }

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password) {
    }

    public record AdminResponse(String username) {
    }
}
