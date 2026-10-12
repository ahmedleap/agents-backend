package com.agentsbackend.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.util.Set;

/**
 * Verifies RS256 access/refresh tokens issued by agents-middleware.
 * Needs only the public key, so the backend never holds signing material.
 */
@Component
public class JwtSignatureVerifier {

    private static final String ALGORITHM = "RS256";
    private static final Set<String> ROLES = Set.of("CLIENT", "ANALYST", "ADMIN");

    private final JwtParser parser;

    public JwtSignatureVerifier(
            @Value("${jwt.public-key}") String publicKeyValue,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience,
            @Value("${jwt.clock-skew-seconds:30}") long clockSkewSeconds) {

        PublicKey publicKey = RsaKeys.parsePublicKey(publicKeyValue);

        // verifyWith(PublicKey) checks the signature and the expiry (exp) and rejects
        // tokens with alg="none" or HS256. requireIssuer/requireAudience enforce the claims.
        this.parser = Jwts.parser()
                .verifyWith(publicKey)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .clockSkewSeconds(clockSkewSeconds)
                .build();
    }

    /**
     * Returns the verified claims, or throws TokenVerificationException.
     */
    public Claims verify(String token) {
        if (token == null || token.isBlank()) {
            throw new TokenVerificationException("MISSING_TOKEN", "token is empty");
        }

        Jws<Claims> jws;
        try {
            jws = parser.parseSignedClaims(token);
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            throw new TokenVerificationException("EXPIRED", "token expired", e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new TokenVerificationException("INVALID", "signature or claims check failed: " + e.getMessage(), e);
        }

        // Pin the algorithm explicitly, in case the library's key-type matching is ever relaxed.
        String alg = jws.getHeader().getAlgorithm();
        if (!ALGORITHM.equals(alg)) {
            throw new TokenVerificationException("INVALID_ALGORITHM", "alg must be RS256, got " + alg);
        }

        Claims claims = jws.getPayload();
        requireString(claims, "sub");
        requireString(claims, "email");
        requireString(claims, "jti");

        String role = claims.get("role", String.class);
        if (role == null || !ROLES.contains(role)) {
            throw new TokenVerificationException("INVALID_CLAIMS", "unknown role: " + role);
        }
        return claims;
    }

    private static void requireString(Claims claims, String name) {
        String value = claims.get(name, String.class);
        if (value == null || value.isEmpty()) {
            throw new TokenVerificationException("MISSING_CLAIMS", "missing claim: " + name);
        }
    }
}