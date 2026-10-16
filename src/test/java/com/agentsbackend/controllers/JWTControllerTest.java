package com.agentsbackend.controllers;

import com.agentsbackend.services.JwtSignatureVerifier;
import com.agentsbackend.services.TokenVerificationException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JWTControllerTest {
    private static final String TOKEN = "synthetic-unit-test-token";
    private final JwtSignatureVerifier verifier = mock(JwtSignatureVerifier.class);
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new JWTController(verifier)).build();
    }

    @Test
    void returnsVerifiedClaimsFromVerifier() throws Exception {
        Claims claims = Jwts.claims()
                .subject("11111111-1111-4111-8111-111111111111")
                .add("email", "client@example.test")
                .add("role", "CLIENT")
                .add("jti", "unit-test-jti")
                .build();
        when(verifier.verify(TOKEN)).thenReturn(claims);

        mockMvc.perform(post("/api/jwt/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content("{\"token\":\"" + TOKEN + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.subject").value("11111111-1111-4111-8111-111111111111"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void rejectsMismatchBetweenAuthorizationAndBodyToken() throws Exception {
        mockMvc.perform(post("/api/jwt/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content("{\"token\":\"different-token\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false));
    }

    @Test
    void mapsVerifierFailureToUnauthorized() throws Exception {
        when(verifier.verify(TOKEN)).thenThrow(
                new TokenVerificationException("INVALID", "signature validation failed"));

        mockMvc.perform(get("/api/jwt/verify-header")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
