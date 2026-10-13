package com.agentsbackend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifies the compact RS256 JWS through Spring's configured JWT decoder,
 * then exposes the already-verified claims using JJWT's Claims API.
 * The signing private key is never loaded by this service.
 */
@Component
public class JwtSignatureVerifier {
    private static final String ALGORITHM = "RS256";

    private final JwtDecoder jwtDecoder;

    public JwtSignatureVerifier(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    public Claims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenVerificationException("MISSING_TOKEN", "token is empty");
        }

        final Jwt verifiedJwt;
        try {
            // JwtDecoder verifies the RS256 signature and configured issuer, audience,
            // and timestamps before any claims are returned.
            verifiedJwt = jwtDecoder.decode(token.trim());
        } catch (JwtException | IllegalArgumentException exception) {
            String reason = String.valueOf(exception.getMessage()).toLowerCase();
            if (reason.contains("expired")) {
                throw new TokenVerificationException("EXPIRED", "token expired", exception);
            }
            throw new TokenVerificationException(
                    "INVALID", "signature or claims validation failed", exception);
        }

        Object algorithm = verifiedJwt.getHeaders().get("alg");
        if (!ALGORITHM.equals(algorithm)) {
            throw new TokenVerificationException(
                    "INVALID_ALGORITHM", "alg must be RS256, got " + algorithm);
        }

        Map<String, Object> claimsMap = new HashMap<>(verifiedJwt.getClaims());
        for (String dateClaim : List.of("iat", "nbf", "exp")) {
            Object value = claimsMap.get(dateClaim);
            if (value instanceof Instant instant) {
                claimsMap.put(dateClaim, Date.from(instant));
            }
        }

        Claims claims = Jwts.claims().add(claimsMap).build();
        return claims;
    }
}
