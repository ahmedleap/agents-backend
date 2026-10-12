package com.agentsbackend.controllers;

import com.agentsbackend.dto.VerifyTokenRequest;
import com.agentsbackend.dto.VerifyTokenResponse;
import com.agentsbackend.services.JwtSignatureVerifier;
import com.agentsbackend.services.TokenVerificationException;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for JWT token verification endpoints.
 * Provides endpoints to test asymmetric RSA key-based token verification.
 */
@RestController
@RequestMapping("/api/jwt")
public class JWTController {

    private final JwtSignatureVerifier jwtSignatureVerifier;

    public JWTController(JwtSignatureVerifier jwtSignatureVerifier) {
        this.jwtSignatureVerifier = jwtSignatureVerifier;
    }

    /**
     * Verify a JWT token using the asymmetric public key.
     * 
     * Usage in Bruno:
     * POST http://localhost:8080/api/jwt/verify
     * Content-Type: application/json
     * 
     * Body:
     * {
     *   "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
     * }
     */
    @PostMapping("/verify")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@RequestBody VerifyTokenRequest request) {
        try {
            String token = request.getToken();
            if (token == null || token.isBlank()) {
                return ResponseEntity.badRequest().body(new VerifyTokenResponse(
                        false,
                        "Token is required",
                        null,
                        null,
                        null,
                        null
                ));
            }

            // Strip "Bearer " prefix if present
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // Verify the token using the public key
            Claims claims = jwtSignatureVerifier.verify(token);

            // Extract key claims
            String subject = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Convert all claims to a map for the response
            Map<String, Object> claimsMap = new HashMap<>(claims);

            VerifyTokenResponse response = new VerifyTokenResponse(
                    true,
                    "Token verification successful - Asymmetric RSA key signature valid!",
                    claimsMap,
                    subject,
                    email,
                    role
            );

            return ResponseEntity.ok(response);

        } catch (TokenVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new VerifyTokenResponse(
                    false,
                    "Token verification failed: " + e.getCode() + " - " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VerifyTokenResponse(
                    false,
                    "Error verifying token: " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            ));
        }
    }

    /**
     * Verify a JWT token using Authorization header.
     * 
     * Usage in Bruno:
     * GET http://localhost:8080/api/jwt/verify-header
     * Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
     */
    @GetMapping("/verify-header")
    public ResponseEntity<VerifyTokenResponse> verifyTokenFromHeader(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(new VerifyTokenResponse(
                        false,
                        "Authorization header with 'Bearer <token>' format is required",
                        null,
                        null,
                        null,
                        null
                ));
            }

            String token = authHeader.substring(7);

            // Verify the token using the public key
            Claims claims = jwtSignatureVerifier.verify(token);

            // Extract key claims
            String subject = claims.getSubject();
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);

            // Convert all claims to a map for the response
            Map<String, Object> claimsMap = new HashMap<>(claims);

            VerifyTokenResponse response = new VerifyTokenResponse(
                    true,
                    "Token verification successful - Asymmetric RSA key signature valid!",
                    claimsMap,
                    subject,
                    email,
                    role
            );

            return ResponseEntity.ok(response);

        } catch (TokenVerificationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new VerifyTokenResponse(
                    false,
                    "Token verification failed: " + e.getCode() + " - " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VerifyTokenResponse(
                    false,
                    "Error verifying token: " + e.getMessage(),
                    null,
                    null,
                    null,
                    null
            ));
        }
    }

    /**
     * Health check endpoint to verify JWT configuration is loaded.
     * 
     * Usage in Bruno:
     * GET http://localhost:8080/api/jwt/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "JWT verifier is running");
        response.put("algorithm", "RS256");
        response.put("message", "Ready to verify asymmetric RSA-signed tokens");
        return ResponseEntity.ok(response);
    }
}

