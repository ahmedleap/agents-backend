package com.agentsbackend.config;

import com.agentsbackend.controllers.AccountController;
import com.agentsbackend.controllers.AdminController;
import com.agentsbackend.controllers.OrderController;
import com.agentsbackend.services.AccountService;
import com.agentsbackend.services.AdminService;
import com.agentsbackend.services.OrderService;
import com.agentsbackend.services.MissionAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.List;
import java.time.Instant;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = JwtRoleAuthorizationTest.TestApplication.class)
@TestPropertySource(properties = {
        "auth.jwt.jwks-uri=https://auth.example.test/.well-known/jwks.json",
        "auth.jwt.issuer=https://auth.example.test",
        "auth.jwt.audience=mission-service"
})
class JwtRoleAuthorizationTest {
    @Autowired private WebApplicationContext applicationContext;
    @Autowired private AccountService accountService;
        @Autowired private MissionAuthorizationService authorizationService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
    }

    @Test
    void convertsNestRolesClaimToSpringAuthorities() {
        var token = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("synthetic-only")
                .header("alg", "RS256")
                .subject("11111111-1111-4111-8111-111111111111")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("roles", List.of("CLIENT", "ANALYST"))
                .build();

        var authorities = new JwtSecurityConfig().jwtAuthenticationConverter().convert(token)
                .getAuthorities().stream().map(authority -> authority.getAuthority()).toList();

        assertEquals(List.of("ROLE_CLIENT", "ROLE_ANALYST"), authorities);
    }

    @Test
    void clientCanUseClientAccountApiButCannotUseAdminApi() throws Exception {
        when(accountService.listAccounts(java.util.UUID.fromString("11111111-1111-4111-8111-111111111111")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/accounts")
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admins/create")
                        .contentType("application/json")
                        .content("{}")
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseAdminApiButCannotSubmitTrades() throws Exception {
        mockMvc.perform(post("/api/admins/create")
                        .contentType("application/json")
                        .content("{}")
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(validOrder())
                        .with(jwtWithRole("ADMIN")))
                .andExpect(status().isForbidden());
    }

    @Test
    void clientCanSubmitTradeAndAnonymousRequestIsRejected() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(validOrder())
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/accounts"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void analystCanReadButCannotCreateOrders() throws Exception {
        mockMvc.perform(get("/api/v1/orders")
                        .param("accountId", "11111111-1111-4111-8111-111111111111")
                        .with(jwtWithRole("ANALYST")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType("application/json")
                        .content(validOrder())
                        .with(jwtWithRole("ANALYST")))
                .andExpect(status().isForbidden());
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt.subject("11111111-1111-4111-8111-111111111111")
                        .claim("roles", List.of(role)))
                .authorities(new SimpleGrantedAuthority("ROLE_" + role));
    }

    private String validOrder() {
        return """
                {
                  "accountId": "11111111-1111-4111-8111-111111111111",
                  "instrumentId": "22222222-2222-4222-8222-222222222222",
                  "quantity": 1,
                  "orderType": "BUY"
                }
                """;
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({JwtSecurityConfig.class, AccountController.class, AdminController.class, OrderController.class})
    static class TestApplication {
        @Bean AccountService accountService() { return mock(AccountService.class); }
        @Bean AdminService adminService() { return mock(AdminService.class); }
        @Bean OrderService orderService() { return mock(OrderService.class); }
                @Bean MissionAuthorizationService missionAuthorizationService() {
                        return mock(MissionAuthorizationService.class);
                }
    }
}
