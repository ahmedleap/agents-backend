package com.agentsbackend.controllers;

import com.agentsbackend.config.JwtSecurityConfig;
import com.agentsbackend.services.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = JWTControllerTest.TestApplication.class)
@TestPropertySource(properties = {
        "auth.jwt.issuer=agents-middleware",
        "auth.jwt.audience=agents-backend",
        "auth.session-validation.enabled=true"
})
class JWTControllerTest {
    private static final String TOKEN = "synthetic-unit-test-token";
    private static final UUID SUBJECT = UUID.fromString("11111111-1111-4111-8111-111111111111");
    private static final UUID STAFF_SUBJECT = UUID.fromString("22222222-2222-4222-8222-222222222222");
    @Autowired private WebApplicationContext context;
    @Autowired private JwtDecoder decoder;
    @Autowired private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(decoder, authService);
        when(decoder.decode(TOKEN)).thenReturn(token("CLIENT"));
        when(authService.isSessionValid(TOKEN, SUBJECT)).thenReturn(true);
        mockMvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @Test
    void returnsPrincipalClaimsAndDecodesOnlyOnce() throws Exception {
        mockMvc.perform(post("/api/jwt/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + TOKEN)
                        .content("{\"token\":\"" + TOKEN + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.subject").value("11111111-1111-4111-8111-111111111111"))
                .andExpect(jsonPath("$.role").value("CLIENT"))
                .andExpect(jsonPath("$.email").value("client@example.test"))
                .andExpect(jsonPath("$.claims.internalOnly").doesNotExist());

            verify(decoder, times(1)).decode(TOKEN);
            verify(authService).isSessionValid(TOKEN, SUBJECT);
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
    void rejectsInvalidTokenBeforeSessionLookup() throws Exception {
        when(decoder.decode(TOKEN)).thenThrow(new BadJwtException("Invalid signature"));

        mockMvc.perform(get("/api/jwt/verify-header")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(authService);
    }

    @Test
    void reportsHeaderPrincipalWithRolesFallbackWithoutDecodingAgain() throws Exception {
        when(decoder.decode(TOKEN)).thenReturn(Jwt.withTokenValue(TOKEN)
                .header("alg", "RS256").subject(SUBJECT.toString())
                .claim("roles", List.of("ADMIN")).claim("email", "staff@example.test").build());

        mockMvc.perform(get("/api/jwt/verify-header").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
        verify(decoder, times(1)).decode(TOKEN);
    }

    @Test
    void rejectsMissingAuthentication() throws Exception {
        mockMvc.perform(get("/api/jwt/verify-header")).andExpect(status().isUnauthorized());
        verifyNoInteractions(decoder, authService);
    }

    @Test
    void rejectsRevokedSessionEvenWithVerifiedJwt() throws Exception {
        when(authService.isSessionValid(TOKEN, SUBJECT)).thenReturn(false);
        mockMvc.perform(get("/api/jwt/verify-header").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void preservesAnalystEndpointRestriction() throws Exception {
        when(decoder.decode(TOKEN)).thenReturn(token("ANALYST"));
        mockMvc.perform(get("/api/jwt/verify-header").header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isForbidden());
    }

    @Test
    void validatesStaffSessionUsingJwtSubjectUuid() throws Exception {
        when(decoder.decode(TOKEN)).thenReturn(token("ADMIN", STAFF_SUBJECT, "staff@example.test"));
        when(authService.isSessionValid(TOKEN, STAFF_SUBJECT)).thenReturn(true);

        mockMvc.perform(get("/api/jwt/verify-header")
                        .header("Authorization", "Bearer " + TOKEN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value(STAFF_SUBJECT.toString()))
                .andExpect(jsonPath("$.role").value("ADMIN"));

        verify(authService).isSessionValid(TOKEN, STAFF_SUBJECT);
    }

    @Test
    void rejectsMissingBodyToken() throws Exception {
        mockMvc.perform(post("/api/jwt/verify").header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.valid").value(false));
    }

    private Jwt token(String role) {
        return token(role, SUBJECT, "client@example.test");
    }

    private Jwt token(String role, UUID subject, String email) {
        return Jwt.withTokenValue(TOKEN).header("alg", "RS256")
                .subject(subject.toString()).issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("role", role).claim("email", email)
                .claim("jti", "unit-test-jti").claim("internalOnly", "not exposed").build();
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({JWTController.class, TestSecurityConfig.class})
    static class TestApplication {
        @Bean AuthService authService() { return mock(AuthService.class); }
    }

    @Configuration
    static class TestSecurityConfig extends JwtSecurityConfig {
        @Override
        @Bean
        public JwtDecoder jwtDecoder(
                @org.springframework.beans.factory.annotation.Value("${auth.jwt.public-key-file:}") String file,
                @org.springframework.beans.factory.annotation.Value("${auth.jwt.public-key-base64:}") String key,
                @org.springframework.beans.factory.annotation.Value("${auth.jwt.issuer}") String issuer,
                @org.springframework.beans.factory.annotation.Value("${auth.jwt.audience}") String audience) {
            return mock(JwtDecoder.class);
        }
    }
}
