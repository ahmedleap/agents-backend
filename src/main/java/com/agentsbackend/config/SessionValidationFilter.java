package com.agentsbackend.config;

import com.agentsbackend.services.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/** Rejects verified JWTs that have no live auth_sessions row. Not a bean, so it only runs in the security chain. */
public class SessionValidationFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public SessionValidationFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth && jwtAuth.isAuthenticated()) {
            boolean valid;
            try {
                valid = authService.isSessionValid(
                        jwtAuth.getToken().getTokenValue(), UUID.fromString(jwtAuth.getToken().getSubject()));
            } catch (IllegalArgumentException exception) {
                valid = false;
            }

            if (!valid) {
                SecurityContextHolder.clearContext();
                response.setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
