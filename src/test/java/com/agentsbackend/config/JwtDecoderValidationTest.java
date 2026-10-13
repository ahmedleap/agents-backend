package com.agentsbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtDecoderValidationTest {
    private static final String ISSUER = "agents-middleware";
    private static final String AUDIENCE = "agents-backend";
    private static final String SUBJECT = "11111111-1111-4111-8111-111111111111";
    private static final String KEY_ID = "jwt-decoder-test-key";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private KeyPair trustedKeyPair;
    private JwtDecoder decoder;
    private HttpServer jwksServer;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        trustedKeyPair = keyPairGenerator.generateKeyPair();
        jwksServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        jwksServer.createContext("/.well-known/jwks.json", exchange -> {
            try {
                byte[] response = jwksJson((RSAPublicKey) trustedKeyPair.getPublic())
                        .getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length);
                try (var body = exchange.getResponseBody()) {
                    body.write(response);
                }
            } catch (Exception exception) {
                throw new IOException("Could not serialize test JWKS", exception);
            }
        });
        jwksServer.start();
        String jwksUri = "http://127.0.0.1:" + jwksServer.getAddress().getPort()
                + "/.well-known/jwks.json";
        decoder = new JwtSecurityConfig().jwtDecoder(jwksUri, "", "", ISSUER, AUDIENCE);
    }

    @AfterEach
    void tearDown() {
        if (jwksServer != null) jwksServer.stop(0);
    }

    @Test
    void acceptsValidRs256TokenFromJwks() throws Exception {
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
                .jwtDecoder("http://127.0.0.1:1/unused", publicKeyFile.toString(), "", ISSUER, AUDIENCE);

        Jwt jwt = staticKeyDecoder.decode(signedToken(trustedKeyPair, ISSUER, AUDIENCE, SUBJECT,
            Instant.now().minusSeconds(1), Instant.now().plusSeconds(300)));

        assertEquals(SUBJECT, jwt.getSubject());
            Files.deleteIfExists(publicKeyFile);
        }

        @Test
        void loadsSuppliedWorkspacePublicKeyFile() {
            JwtDecoder fileDecoder = new JwtSecurityConfig().jwtDecoder(
                    "http://127.0.0.1:1/unused",
                    Path.of("public_key.pem").toAbsolutePath().toString(),
                    "",
                    ISSUER,
                    AUDIENCE);

            assertNotNull(fileDecoder);
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
        claims.put("roles", List.of("CLIENT"));

        String unsigned = base64Url(objectMapper.writeValueAsBytes(header)) + "."
                + base64Url(objectMapper.writeValueAsBytes(claims));
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(signer.getPrivate());
        signature.update(unsigned.getBytes(StandardCharsets.US_ASCII));
        return unsigned + "." + base64Url(signature.sign());
    }

    private String jwksJson(RSAPublicKey publicKey) throws Exception {
        Map<String, Object> jwk = new LinkedHashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("use", "sig");
        jwk.put("alg", "RS256");
        jwk.put("kid", KEY_ID);
        jwk.put("n", base64Url(unsignedBytes(publicKey.getModulus())));
        jwk.put("e", base64Url(unsignedBytes(publicKey.getPublicExponent())));
        return objectMapper.writeValueAsString(Map.of("keys", List.of(jwk)));
    }

    private byte[] unsignedBytes(BigInteger integer) {
        byte[] bytes = integer.toByteArray();
        if (bytes.length > 1 && bytes[0] == 0) {
            return java.util.Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return bytes;
    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
