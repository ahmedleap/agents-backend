
package com.agentsbackend.config;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class JwtSecurityConfig {

    @Value("${auth.jwt.public-key}")
    private String encodedPublicKey;

    @Value("${auth.jwt.issuer}")
    private String issuer;

    @Value("${auth.jwt.audience}")
    private String audience;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/accounts/**").hasRole("CLIENT")
                .requestMatchers("/api/watchlists/**").hasRole("CLIENT")
                .requestMatchers("/api/admins/**").hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/v1/orders/**"
                ).hasRole("CLIENT")

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/v1/orders/**"
                ).hasAnyRole("CLIENT", "ADMIN", "ANALYST")

                .requestMatchers("/api/**", "/users/**")
                    .authenticated()

                .anyRequest().permitAll()
            )
            .oauth2ResourceServer(oauth ->
                oauth.jwt(jwt ->
                    jwt.jwtAuthenticationConverter(
                        jwtAuthenticationConverter()
                    )
                )
            );

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {

        // Decode the outer Base64 string into PEM format.
        String pem = new String(
            Base64.getDecoder().decode(encodedPublicKey.trim()),
            StandardCharsets.UTF_8
        );

        // Remove PEM headers and whitespace.
        String keyContent = pem
            .replace("-----BEGIN PUBLIC KEY-----", "")
            .replace("-----END PUBLIC KEY-----", "")
            .replaceAll("\\s", "");

        // Decode the RSA public key.
        byte[] keyBytes = Base64.getDecoder().decode(keyContent);

        RSAPublicKey publicKey = (RSAPublicKey) KeyFactory
            .getInstance("RSA")
            .generatePublic(new X509EncodedKeySpec(keyBytes));

        // Verify JWT signatures using RS256.
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withPublicKey(publicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build();

        // Validate issuer, expiration, and audience.
        OAuth2TokenValidator<Jwt> issuerAndTime =
            JwtValidators.createDefaultWithIssuer(issuer);

        OAuth2TokenValidator<Jwt> audienceValidator =
            new JwtClaimValidator<List<String>>(
                "aud",
                audiences -> audiences != null
                    && audiences.contains(audience)
            );

        // Require a valid UUID subject.
        OAuth2TokenValidator<Jwt> subjectValidator = jwt -> {
            try {
                UUID.fromString(jwt.getSubject());
                return OAuth2TokenValidatorResult.success();
            } catch (Exception e) {
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error(
                        "invalid_token",
                        "JWT subject must be a valid UUID",
                        null
                    )
                );
            }
        };

        decoder.setJwtValidator(
            new DelegatingOAuth2TokenValidator<>(
                issuerAndTime,
                audienceValidator,
                subjectValidator
            )
        );

        return decoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtAuthenticationConverter converter =
            new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            Collection<String> roles = jwt.getClaimAsStringList("roles");

            if (roles == null || roles.isEmpty()) {
                String singleRole = jwt.getClaimAsString("role");

                roles = singleRole == null
                    ? List.of()
                    : List.of(singleRole);
            }

            return roles.stream()
                .filter(role -> List.of(
                    "CLIENT",
                    "ADMIN",
                    "ANALYST"
                ).contains(role))
                .map(role -> (GrantedAuthority)
                    new SimpleGrantedAuthority("ROLE_" + role))
                .toList();
        });

        return converter;
    }
}
