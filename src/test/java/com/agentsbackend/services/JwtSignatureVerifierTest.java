package com.agentsbackend.services;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtSignatureVerifierTest {
    private static final String TOKEN = "synthetic-test-token";

    @Test
    void returnsClaimsOnlyAfterConfiguredDecoderAcceptsToken() {
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode(TOKEN)).thenReturn(jwt("RS256", Map.of(
                "sub", "11111111-1111-4111-8111-111111111111",
                "email", "client@example.test",
                "jti", "test-jti",
                "role", "CLIENT")));

        var claims = new JwtSignatureVerifier(decoder).verify(TOKEN);

        assertEquals("11111111-1111-4111-8111-111111111111", claims.getSubject());
        assertEquals("client@example.test", claims.get("email", String.class));
        assertEquals("test-jti", claims.get("jti", String.class));
    }

    @Test
    void rejectsBlankTokensBeforeDecoderInvocation() {
        JwtSignatureVerifier verifier = new JwtSignatureVerifier(mock(JwtDecoder.class));

        TokenVerificationException exception = assertThrows(TokenVerificationException.class,
                () -> verifier.verify("  "));

        assertEquals("MISSING_TOKEN", exception.getCode());
    }

    @Test
    void rejectsNonRs256Algorithm() {
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode(TOKEN)).thenReturn(jwt("HS256", Map.of(
                "sub", "11111111-1111-4111-8111-111111111111",
                "email", "client@example.test",
                "jti", "test-jti",
                "role", "CLIENT")));

        TokenVerificationException exception = assertThrows(TokenVerificationException.class,
                () -> new JwtSignatureVerifier(decoder).verify(TOKEN));

        assertEquals("INVALID_ALGORITHM", exception.getCode());
    }

    @Test
    void acceptsVerifiedTokenBeforeNestSpecificClaimsAreConfirmed() {
        JwtDecoder decoder = mock(JwtDecoder.class);
        when(decoder.decode(TOKEN)).thenReturn(jwt("RS256", Map.of(
                "sub", "11111111-1111-4111-8111-111111111111")));

        var claims = new JwtSignatureVerifier(decoder).verify(TOKEN);

        assertEquals("11111111-1111-4111-8111-111111111111", claims.getSubject());
    }

    private Jwt jwt(String algorithm, Map<String, Object> claims) {
        return Jwt.withTokenValue(TOKEN)
                .header("alg", algorithm)
                .subject((String) claims.get("sub"))
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claims(values -> values.putAll(claims))
                .build();
    }
}
