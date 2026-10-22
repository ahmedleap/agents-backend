package com.agentsbackend.controllers;

import com.agentsbackend.DTO.VerifyTokenRequest;
import com.agentsbackend.DTO.VerifyTokenResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.time.Instant;
import java.util.Date;

/** Authenticated diagnostic endpoints for Bruno/JWT signature testing. */
@RestController
@RequestMapping("/api/jwt")
@Tag(name = "Authentication", description = "JWT verification endpoints; login is handled by the external identity provider")
@SecurityRequirement(name = "bearerAuth")
public class JWTController {
    /**
     * Reports claims already verified by Spring Security. The body must contain
     * the same token as the authenticated principal; it is never decoded again.
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify a JWT", description = "Verifies that the bearer token and request-body token match and that the JWT signature and claims are valid.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token verified"),
        @ApiResponse(responseCode = "400", description = "Missing token or token mismatch"),
        @ApiResponse(responseCode = "401", description = "Token signature or claims are invalid")
    })
    public ResponseEntity<VerifyTokenResponse> verifyToken(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Bearer token used for resource-server authentication", required = true)
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "JWT to verify; must match the Authorization bearer token",
                content = @Content(schema = @Schema(implementation = VerifyTokenRequest.class))
            )
            @RequestBody(required = false) VerifyTokenRequest request) {
        if (jwt == null) {
            return unauthorized();
        }
        String headerToken = bearerToken(authorization);
        String bodyToken = request == null ? null : request.getToken();
        if (bodyToken == null || bodyToken.isBlank()) {
            return ResponseEntity.badRequest().body(failure("MISSING_TOKEN", "Token is required"));
        }
        if (headerToken == null || !jwt.getTokenValue().equals(headerToken)
            || !jwt.getTokenValue().equals(bodyToken.trim())) {
            return ResponseEntity.badRequest().body(failure(
                    "TOKEN_MISMATCH", "Body token must match the Authorization Bearer token"));
        }
        return verifiedResponse(jwt);
    }

    /** Verify the JWT already authenticated from the Authorization header. */
    @GetMapping("/verify-header")
    @Operation(summary = "Verify the authenticated JWT", description = "Verifies the JWT supplied in the Authorization bearer header.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Token verified"),
        @ApiResponse(responseCode = "400", description = "Missing or malformed bearer token"),
        @ApiResponse(responseCode = "401", description = "Token signature or claims are invalid")
    })
    public ResponseEntity<VerifyTokenResponse> verifyTokenFromHeader(
            @AuthenticationPrincipal Jwt jwt,
            @Parameter(description = "Bearer token to verify", required = true)
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (jwt == null) {
            return unauthorized();
        }
        String token = bearerToken(authorization);
        if (token == null) {
            return ResponseEntity.badRequest().body(failure(
                    "MISSING_TOKEN", "Authorization header must use Bearer <token> format"));
        }
        if (!jwt.getTokenValue().equals(token)) {
            return ResponseEntity.badRequest().body(failure(
                    "TOKEN_MISMATCH", "Header token must match the authenticated token"));
        }
        return verifiedResponse(jwt);
    }

    private ResponseEntity<VerifyTokenResponse> verifiedResponse(Jwt jwt) {
            Map<String, Object> claims = jwt.getClaims();
            Map<String, Object> safeClaims = new HashMap<>();
            for (String name : new String[]{"iss", "aud", "sub", "email", "jti", "role", "roles", "iat", "nbf", "exp"}) {
                Object value = claims.get(name);
                // Preserve the existing diagnostic response's date serialization.
                if (value instanceof Instant instant) value = Date.from(instant);
                if (value != null) safeClaims.put(name, value);
            }
            String role = jwt.getClaimAsString("role");
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
                    jwt.getSubject(), jwt.getClaimAsString("email"), role));
    }

    private ResponseEntity<VerifyTokenResponse> unauthorized() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(failure("MISSING_AUTHENTICATION", "A verified JWT is required"));
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
