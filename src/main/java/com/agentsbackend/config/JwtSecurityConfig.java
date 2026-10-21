
package com.agentsbackend.config;

import java.util.Collection;
import java.util.Base64;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        boolean bootstrapEnabled = adminBootstrapEnabled;

        http
            // Use the CorsConfig rules so browser preflight (OPTIONS) requests aren't rejected before auth
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .authorizeHttpRequests(auth -> {
                if (bootstrapEnabled) {
                    auth.requestMatchers(HttpMethod.POST, "/api/admins").permitAll();
                }

                auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                .requestMatchers(
                    "/api/accounts/**",
                    "/api/watchlists/**"
                ).hasRole("CLIENT")

                .requestMatchers("/api/jwt/**").authenticated()

                .requestMatchers("/api/admins/**")
                    .hasRole("ADMIN")

                .requestMatchers(
                    "/admin/**",
                    "/api/v1/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers("/api/v1/analytics/**")
                    .hasRole("ANALYST")

                .requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()

                .requestMatchers(
                    "/api/portfolio/**",
                    "/api/holding/**"
                ).hasRole("CLIENT")

                .requestMatchers("/dashboard")
                    .hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/orders/**"
                ).hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/orders/**"
                ).hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/orders/**"
                ).hasAnyRole("CLIENT", "ADMIN", "ANALYST")

                // All remaining API and user routes require a JWT.
                .requestMatchers("/users/**")
                    .hasRole("CLIENT")

                .requestMatchers("/api/**")
                    .authenticated()

                // Public endpoints must be explicitly reviewed.
                .anyRequest().denyAll();
            })
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    @Value("${app.admin-bootstrap.enabled:false}")
    private boolean adminBootstrapEnabled;

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${auth.jwt.public-key-file:}") String publicKeyFile,
            @Value("${auth.jwt.public-key-base64:}") String publicKeyBase64,
            @Value("${auth.jwt.issuer}") String issuer,
            @Value("${auth.jwt.audience}") String audience) {

        NimbusJwtDecoder decoder;
        if (publicKeyFile != null && !publicKeyFile.isBlank()) {
            decoder = NimbusJwtDecoder
                .withPublicKey(readRsaPublicKey(publicKeyFile))
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        } else if (publicKeyBase64 != null && !publicKeyBase64.isBlank()) {
            decoder = NimbusJwtDecoder
                .withPublicKey(decodeRsaPublicKey(publicKeyBase64))
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();
        } else {
            throw new IllegalStateException(
                "A static RSA public key is required via auth.jwt.public-key-file or " +
                "auth.jwt.public-key-base64"
            );
        }

        OAuth2TokenValidator<Jwt> issuerAndTime =
            JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
            jwt.getAudience() != null && jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Required audience is missing", null)
                );

        OAuth2TokenValidator<Jwt> requiredClaimsValidator = jwt -> {
            boolean hasRole = jwt.getClaimAsString("role") != null
                || (jwt.getClaimAsStringList("roles") != null
                    && !jwt.getClaimAsStringList("roles").isEmpty());
            boolean valid = jwt.getSubject() != null
                && !jwt.getSubject().isBlank()
                && jwt.getClaimAsString("email") != null
                && !jwt.getClaimAsString("email").isBlank()
                && jwt.getId() != null
                && !jwt.getId().isBlank()
                && jwt.getIssuedAt() != null
                && jwt.getExpiresAt() != null
                && hasRole;

            return valid
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "Required JWT claims are missing",
                        null
                    )
                );
        };

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerAndTime,
                audienceValidator,
                requiredClaimsValidator
            )
        );

        return decoder;
    }

    private RSAPublicKey readRsaPublicKey(String publicKeyFile) {
        try {
            return decodeRsaPublicKey(Files.readString(Path.of(publicKeyFile)));
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException(
                "Could not load RSA public key from auth.jwt.public-key-file: " + publicKeyFile,
                exception
            );
        }
    }

    private RSAPublicKey decodeRsaPublicKey(String configuredKey) {
        try {
            String keyText = configuredKey.trim();
            if (!keyText.startsWith("-----BEGIN")) {
                keyText = new String(Base64.getDecoder().decode(keyText), StandardCharsets.UTF_8).trim();
            }

            String encodedDer = keyText
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
            byte[] der = Base64.getDecoder().decode(encodedDer);
            RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(der));
            if (publicKey.getModulus().bitLength() < 2048) {
                throw new IllegalStateException("JWT RSA public key must be at least 2048 bits");
            }
            return publicKey;
        } catch (IllegalArgumentException | GeneralSecurityException exception) {
            throw new IllegalStateException("auth.jwt.public-key-base64 must contain a Base64-encoded PEM RSA public key", exception);
        }
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<String> roles =
                jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                String singleRole =
                    jwt.getClaimAsString("role");

                roles = singleRole == null
                    ? List.of()
                    : List.of(singleRole);
            }

            return roles.stream()
                .filter(role -> role != null)
                .map(role -> role.trim().toUpperCase(java.util.Locale.ROOT))
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .filter(role ->
                    List.of(
                        "CLIENT",
                        "ADMIN",
                        "ANALYST"
                    ).contains(role)
                )
                .map(role -> (GrantedAuthority)
                    new SimpleGrantedAuthority(
                        "ROLE_" + role
                    )
                )
                .toList();
        });

        return converter;
    }
}
