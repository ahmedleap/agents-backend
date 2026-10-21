
package com.agentsbackend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.agentsbackend.services.AuthService;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class JwtSecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ObjectProvider<AuthService> authService,
            @Value("${auth.session-validation.enabled:true}") boolean sessionValidation)
            throws Exception {

        if (sessionValidation) {
            http.addFilterAfter(new SessionValidationFilter(authService.getObject()),
                BearerTokenAuthenticationFilter.class);
        }

        http
            // Allow Angular frontend CORS requests.
            .cors(Customizer.withDefaults())

            // Preserve existing stateless JWT security.
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Client endpoints
                .requestMatchers(
                    "/api/accounts/**",
                    "/api/watchlists/**"
                ).hasRole("CLIENT")

                // JWT protected endpoints
                .requestMatchers("/api/jwt/**")
                    .hasAnyRole("CLIENT", "ADMIN")

                // Administrator endpoints
                .requestMatchers("/api/admins/**")
                    .hasRole("ADMIN")

                .requestMatchers(
                    "/admin/**",
                    "/api/v1/admin/**"
                ).hasRole("ADMIN")

                // Analytics endpoints
                .requestMatchers("/api/v1/analytics/**")
                    .hasRole("ANALYST")

                // Portfolio and holdings
                .requestMatchers(
                    "/api/portfolio/**",
                    "/api/holding/**"
                ).hasRole("CLIENT")

                // Dashboard
                .requestMatchers("/dashboard")
                    .hasRole("CLIENT")

                // Order creation
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/orders/**"
                ).hasRole("CLIENT")

                // Order deletion
                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/v1/orders/**"
                ).hasRole("CLIENT")

                // Order retrieval
                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/orders/**"
                ).hasAnyRole("CLIENT", "ADMIN")

                // User endpoints
                .requestMatchers("/users/**")
                    .hasRole("CLIENT")

                // Remaining API endpoints require JWT.
                .requestMatchers("/api/**")
                    .hasAnyRole("CLIENT", "ADMIN")

                // Deny all unspecified routes.
                .anyRequest().denyAll()
            )

            // JWT authentication
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    // CORS configuration for Angular frontend.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(
            List.of("http://localhost:4200")
        );

        config.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            )
        );

        config.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type"
            )
        );

        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }

    // JWT decoder using RSA public key.
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

        } else if (publicKeyBase64 != null
                && !publicKeyBase64.isBlank()) {

            decoder = NimbusJwtDecoder
                .withPublicKey(decodeRsaPublicKey(publicKeyBase64))
                .signatureAlgorithm(SignatureAlgorithm.RS256)
                .build();

        } else {

            throw new IllegalStateException(
                "A static RSA public key is required via " +
                "auth.jwt.public-key-file or " +
                "auth.jwt.public-key-base64"
            );
        }

        // Validate issuer and standard time claims.
        OAuth2TokenValidator<Jwt> issuerAndTime =
            JwtValidators.createDefaultWithIssuer(issuer);

        // Validate intended audience.
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
            jwt.getAudience() != null
                && jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "Required audience is missing",
                        null
                    )
                );

        // Preserve required JWT claim validation.
        OAuth2TokenValidator<Jwt> requiredClaimsValidator = jwt -> {

            boolean hasRole =
                jwt.getClaimAsString("role") != null
                || (
                    jwt.getClaimAsStringList("roles") != null
                    && !jwt.getClaimAsStringList("roles").isEmpty()
                );

            boolean valid =
                jwt.getSubject() != null
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

    // Load RSA public key from file.
    private RSAPublicKey readRsaPublicKey(String publicKeyFile) {

        try {
            return decodeRsaPublicKey(
                Files.readString(Path.of(publicKeyFile))
            );

        } catch (IOException | RuntimeException exception) {

            throw new IllegalStateException(
                "Could not load RSA public key from " +
                "auth.jwt.public-key-file: " + publicKeyFile,
                exception
            );
        }
    }

    // Decode and validate RSA public key.
    private RSAPublicKey decodeRsaPublicKey(String configuredKey) {

        try {
            String keyText = configuredKey.trim();

            if (!keyText.startsWith("-----BEGIN")) {

                keyText = new String(
                    Base64.getDecoder().decode(keyText),
                    StandardCharsets.UTF_8
                ).trim();
            }

            String encodedDer = keyText
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");

            byte[] der =
                Base64.getDecoder().decode(encodedDer);

            RSAPublicKey publicKey =
                (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(
                        new X509EncodedKeySpec(der)
                    );

            if (publicKey.getModulus().bitLength() < 2048) {

                throw new IllegalStateException(
                    "JWT RSA public key must be at least 2048 bits"
                );
            }

            return publicKey;

        } catch (
            IllegalArgumentException |
            GeneralSecurityException exception
        ) {

            throw new IllegalStateException(
                "auth.jwt.public-key-base64 must contain " +
                "a Base64-encoded PEM RSA public key",
                exception
            );
        }
    }

    // Convert JWT roles into Spring Security authorities.
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
                .map(role ->
                    role.trim().toUpperCase(Locale.ROOT)
                )
                .map(role ->
                    role.startsWith("ROLE_")
                        ? role.substring(5)
                        : role
                )
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
