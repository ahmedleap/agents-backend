package com.agentsbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderValidationTest {
    private static final String ISSUER = "agents-middleware";
    private static final String AUDIENCE = "agents-backend";
    private static final String SUBJECT = "11111111-1111-4111-8111-111111111111";
    private static final String KEY_ID = "jwt-decoder-test-key";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KeyPair trustedKeyPair;
    private JwtDecoder decoder;
    private Path publicKeyFile;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        trustedKeyPair = keyPairGenerator.generateKeyPair();
        String pem = "-----BEGIN PUBLIC KEY-----\n"
                + Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(trustedKeyPair.getPublic().getEncoded())
                + "\n-----END PUBLIC KEY-----\n";
        publicKeyFile = Files.createTempFile("jwt-public-key", ".pem");
        Files.writeString(publicKeyFile, pem, StandardCharsets.UTF_8);
        decoder = new JwtSecurityConfig().jwtDecoder(publicKeyFile.toString(), "", ISSUER, AUDIENCE);
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(publicKeyFile);
    }

    @Test
    void acceptsValidRs256TokenFromStaticPublicKey() throws Exception {
        Jwt jwt = decoder.decode(signedToken(trustedKeyPair, ISSUER, AUDIENCE, SUBJECT,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300)));

        assertEquals(SUBJECT, jwt.getSubject());
        assertEquals(ISSUER, jwt.getClaimAsString("iss"));
        assertEquals(List.of(AUDIENCE), jwt.getAudience());
    }

        @Test
            void acceptsValidRs256TokenUsingPemPublicKeyFile() throws Exception {
        String pem = "-----BEGIN PUBLIC KEY-----\n"
            + Base64.getMimeEncoder(64, new byte[]{'\n'})
                .encodeToString(trustedKeyPair.getPublic().getEncoded())
            + "\n-----END PUBLIC KEY-----\n";
            Path publicKeyFile = Files.createTempFile("jwt-public-key", ".pem");
            Files.writeString(publicKeyFile, pem, StandardCharsets.UTF_8);
        JwtDecoder staticKeyDecoder = new JwtSecurityConfig()
            .jwtDecoder(publicKeyFile.toString(), "", ISSUER, AUDIENCE);

        Jwt jwt = staticKeyDecoder.decode(signedToken(trustedKeyPair, ISSUER, AUDIENCE, SUBJECT,
            Instant.now().minusSeconds(1), Instant.now().plusSeconds(300)));

        assertEquals(SUBJECT, jwt.getSubject());
        }

    @Test
    void rejectsWrongIssuer() throws Exception {
        assertThrows(JwtException.class, () -> decoder.decode(signedToken(
                trustedKeyPair, "untrusted-issuer", AUDIENCE, SUBJECT,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300))));
    }

    @Test
    void rejectsWrongAudience() throws Exception {
        assertThrows(JwtException.class, () -> decoder.decode(signedToken(
                trustedKeyPair, ISSUER, "another-service", SUBJECT,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300))));
    }

    @Test
    void rejectsInvalidSignature() throws Exception {
        KeyPair otherKeyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        assertThrows(JwtException.class, () -> decoder.decode(signedToken(
                otherKeyPair, ISSUER, AUDIENCE, SUBJECT,
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300))));
    }

    @Test
    void rejectsExpiredToken() throws Exception {
        assertThrows(JwtException.class, () -> decoder.decode(signedToken(
                trustedKeyPair, ISSUER, AUDIENCE, SUBJECT,
                Instant.now().minusSeconds(60), Instant.now().minusSeconds(1))));
    }

    @Test
    void rejectsTokenNotYetValid() throws Exception {
        assertThrows(JwtException.class, () -> decoder.decode(signedToken(
                trustedKeyPair, ISSUER, AUDIENCE, SUBJECT,
                Instant.now().plusSeconds(120), Instant.now().plusSeconds(300))));
    }

    @Test
    void acceptsSubjectWithoutAssumingUuidFormat() throws Exception {
        Jwt jwt = decoder.decode(signedToken(trustedKeyPair, ISSUER, AUDIENCE, "nest-user-123",
                Instant.now().minusSeconds(1), Instant.now().plusSeconds(300)));

        assertEquals("nest-user-123", jwt.getSubject());
    }

    private String signedToken(KeyPair signer, String issuer, String audience, String subject,
                               Instant notBefore, Instant expiresAt) throws Exception {
        Map<String, Object> header = Map.of("alg", "RS256", "typ", "JWT", "kid", KEY_ID);
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("iss", issuer);
        claims.put("aud", List.of(audience));
        claims.put("sub", subject);
        claims.put("iat", Instant.now().getEpochSecond());
        claims.put("nbf", notBefore.getEpochSecond());
        claims.put("exp", expiresAt.getEpochSecond());
        claims.put("email", "client@example.com");
        claims.put("jti", KEY_ID);
        claims.put("role", "CLIENT");

        String unsigned = base64Url(objectMapper.writeValueAsBytes(header)) + "."
                + base64Url(objectMapper.writeValueAsBytes(claims));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(signer.getPrivate());
        signature.update(unsigned.getBytes(StandardCharsets.US_ASCII));
        return unsigned + "." + base64Url(signature.sign());
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
