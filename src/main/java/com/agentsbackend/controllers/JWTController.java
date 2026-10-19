package com.agentsbackend.controllers;

import com.agentsbackend.DTO.VerifyTokenRequest;
import com.agentsbackend.DTO.VerifyTokenResponse;
import com.agentsbackend.services.JwtSignatureVerifier;
import com.agentsbackend.services.TokenVerificationException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/** Authenticated diagnostic endpoints for Bruno/JWT signature testing. */
@RestController
@RequestMapping("/api/jwt")
public class JWTController {
    private final JwtSignatureVerifier jwtSignatureVerifier;

    public JWTController(JwtSignatureVerifier jwtSignatureVerifier) {
        this.jwtSignatureVerifier = jwtSignatureVerifier;
    }

    /**
     * Requires Bearer auth in the Authorization header (Spring resource-server verification)
     * and the same token in the body (explicit JwtSignatureVerifier verification).
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyTokenResponse> verifyToken(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody(required = false) VerifyTokenRequest request) {
        String headerToken = bearerToken(authorization);
        String bodyToken = request == null ? null : request.getToken();
        if (bodyToken == null || bodyToken.isBlank()) {
            return ResponseEntity.badRequest().body(failure("MISSING_TOKEN", "Token is required"));
        }
        if (headerToken == null || !headerToken.equals(bodyToken.trim())) {
            return ResponseEntity.badRequest().body(failure(
                    "TOKEN_MISMATCH", "Body token must match the Authorization Bearer token"));
        }
        return verify(headerToken);
    }

    /** Verify the JWT already authenticated from the Authorization header. */
    @GetMapping("/verify-header")
    public ResponseEntity<VerifyTokenResponse> verifyTokenFromHeader(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        String token = bearerToken(authorization);
        if (token == null) {
            return ResponseEntity.badRequest().body(failure(
                    "MISSING_TOKEN", "Authorization header must use Bearer <token> format"));
        }
        return verify(token);
    }

    private ResponseEntity<VerifyTokenResponse> verify(String token) {
        try {
            Claims claims = jwtSignatureVerifier.verify(token);
            Map<String, Object> safeClaims = new HashMap<>();
            for (String name : new String[]{"iss", "aud", "sub", "email", "jti", "role", "roles", "iat", "nbf", "exp"}) {
                Object value = claims.get(name);
                if (value != null) safeClaims.put(name, value);
            }
            String role = claims.get("role", String.class);
            if (role == null && claims.get("roles") instanceof Iterable<?> roles) {
                for (Object value : roles) {
                    if (value instanceof String roleValue) {
                        role = roleValue;
                        break;
                    }
                }
            }
            return ResponseEntity.ok(new VerifyTokenResponse(
                    true, "RS256 signature and token claims verified.", safeClaims,
                    claims.getSubject(), claims.get("email", String.class), role));
        } catch (TokenVerificationException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(failure(exception.getCode(), exception.getMessage()));
        }
    }

    private VerifyTokenResponse failure(String code, String message) {
        return new VerifyTokenResponse(false, code + ": " + message, null, null, null, null);
    }

    private String bearerToken(String authorization) {
        if (authorization == null || !authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return null;
        }
        return authorization.substring(7).trim();
    }
}
