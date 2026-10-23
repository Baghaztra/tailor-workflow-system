package com.baghaztra.tailorworkflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtCookieFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final AuthSessionStore sessions;

    public JwtCookieFilter(JwtService jwt, AuthSessionStore sessions) {
        this.jwt = jwt;
        this.sessions = sessions;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = readToken(request, AuthCookie.ACCESS_NAME);
            if (token != null) {
                jwt.verifyAccess(token)
                        .filter(access -> sessions.isActive(access.sessionId()))
                        .ifPresent(access -> {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            access.username(),
                                            null,
                                            List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
                            authentication.setDetails(
                                    new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        });
            }
        }
        chain.doFilter(request, response);
    }

    private String readToken(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
