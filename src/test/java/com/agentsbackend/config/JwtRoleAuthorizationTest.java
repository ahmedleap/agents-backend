package com.agentsbackend.config;

import com.agentsbackend.controllers.AccountController;
import com.agentsbackend.controllers.AdminController;
import com.agentsbackend.controllers.AdminReadController;
import com.agentsbackend.controllers.OrderController;
import com.agentsbackend.controllers.WatchlistController;
import com.agentsbackend.DTO.requests.CreateAdminRequest;
import com.agentsbackend.entities.Admin;
import com.agentsbackend.enums.AdminRole;
import com.agentsbackend.repos.transactional.AccountRepository;
import com.agentsbackend.repos.transactional.OrderRepository;
import com.agentsbackend.services.AccountService;
import com.agentsbackend.services.AdminService;
import com.agentsbackend.services.OrderService;
import com.agentsbackend.services.MissionAuthorizationService;
import com.agentsbackend.services.TransactionsService;
import com.agentsbackend.services.WatchlistService;
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
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
        "auth.jwt.public-key-base64=LS0tLS1CRUdJTiBQVUJMSUMgS0VZLS0tLS0KTUlJQklqQU5CZ2txaGtpRzl3MEJBUUVGQUFPQ0FROEFNSUlCQ2dLQ0FRRUF2ZkJDaDB5cTlWOGVzZ0xxSVBxK0dMM3JYczlUU3BHb3MrUHRKQzl1aWs5dEIzdnMzRGlTM2JTYlJQa1JWUllaVHBBN3gxdzZ3WTZzQWZrUHM0QjREcEFud2tIR2I1TmFWaXZkVDdHaTJHNlRQZ3dYaUJ1Z0ZEQklrMXZGWFBIcUR3bEwzY0VVYkw5ZmptMm5rbjkrdTBjZE5jNmUvYzFZOHZCWmt4bVFNNXRnTWZsT2RYVDgzUFBxSGlOY0w0eXdVakNKaWQwdFJzRGQ2SlNseWd1Qk1ENFNmb0pUMGgxRDF2RzBPNUtiQ2FHN25GNWtnWnFkK09EQzJnMHE2RlQwNGhyby9kdUVnL1pteUdXV1hBem5vQnBFSUxBU294NC9Cc2FvWWtrOHY1SStsZ2hrYWxwekJ1WEdSbE1wSlRmZ2VBU1B5cURRMVdwZmFnamVDeFJweXdJREFRQUIKLS0tLS1FTkQgUFVCTElDIEtFWS0tLS0t",
        "auth.jwt.issuer=agents-middleware",
        "auth.jwt.audience=agents-backend"
})
class JwtRoleAuthorizationTest {
        private static final UUID CLIENT_ID = UUID.fromString("11111111-1111-4111-8111-111111111111");
        private static final UUID OTHER_ACCOUNT_ID = UUID.fromString("33333333-3333-4333-8333-333333333333");
        private static final UUID OTHER_ORDER_ID = UUID.fromString("44444444-4444-4444-8444-444444444444");
        private static final UUID OTHER_CLIENT_ID = UUID.fromString("55555555-5555-4555-8555-555555555555");

    @Autowired private WebApplicationContext applicationContext;
    @Autowired private AccountService accountService;
        @Autowired private AdminService adminService;
        @Autowired private AccountRepository accountRepository;
        @Autowired private OrderRepository orderRepository;
        @Autowired private OrderService orderService;
        @Autowired private WatchlistService watchlistService;
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
    void convertsSingleRoleClaimAndIgnoresUnsupportedRoles() {
        var token = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("synthetic-only")
                .header("alg", "RS256")
                .subject(CLIENT_ID.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("role", "ROLE_CLIENT")
                .claim("roles", List.of("ROOT"))
                .build();

        var authorities = new JwtSecurityConfig().jwtAuthenticationConverter().convert(token)
                .getAuthorities().stream().map(authority -> authority.getAuthority()).toList();

        assertEquals(List.of(), authorities);

        var singleRoleToken = org.springframework.security.oauth2.jwt.Jwt.withTokenValue("synthetic-only")
                .header("alg", "RS256")
                .subject(CLIENT_ID.toString())
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claim("role", "ROLE_CLIENT")
                .build();
        var singleRoleAuthorities = new JwtSecurityConfig().jwtAuthenticationConverter()
                .convert(singleRoleToken).getAuthorities().stream()
                .map(authority -> authority.getAuthority()).toList();

        assertEquals(List.of("ROLE_CLIENT"), singleRoleAuthorities);
    }

    @Test
    void clientCanUseClientAccountApiButCannotUseAdminApi() throws Exception {
        when(accountService.listAccounts(java.util.UUID.fromString("11111111-1111-4111-8111-111111111111")))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/accounts")
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/admins")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Root\",\"lastName\":\"Admin\",\"email\":\"root@example.test\",\"password\":\"correct-horse\",\"role\":\"ADMIN\"}")
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanUseAdminApiButCannotSubmitTrades() throws Exception {
        when(adminService.createAdmin(any(CreateAdminRequest.class))).thenReturn(new Admin(
                UUID.randomUUID(), "Root", "Admin", "root@example.test", "hashed", AdminRole.ADMIN,
                java.time.OffsetDateTime.now()));

        mockMvc.perform(post("/api/admins")
                        .contentType("application/json")
                        .content("{\"firstName\":\"Root\",\"lastName\":\"Admin\",\"email\":\"root@example.test\",\"password\":\"correct-horse\",\"role\":\"ADMIN\"}")
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
                when(accountRepository.existsByIdAndClientId(CLIENT_ID, CLIENT_ID)).thenReturn(true);

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

    @Test
    void clientCannotReadAnotherClientsAccountOrOrder() throws Exception {
        when(accountRepository.existsByIdAndClientId(OTHER_ACCOUNT_ID, CLIENT_ID)).thenReturn(false);
        when(orderRepository.findAccountIdByOrderId(OTHER_ORDER_ID)).thenReturn(OTHER_ACCOUNT_ID);

        mockMvc.perform(get("/api/accounts/{accountId}", OTHER_ACCOUNT_ID)
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/orders/{orderId}", OTHER_ORDER_ID)
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/orders")
                        .param("accountId", OTHER_ACCOUNT_ID.toString())
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isNotFound());

        verify(accountService, never()).getAccountDetails(OTHER_ACCOUNT_ID);
        verify(orderService, never()).getOrderById(OTHER_ORDER_ID);
        verify(orderService, never()).getOrderHistory(OTHER_ACCOUNT_ID);
    }

    @Test
    void clientCannotAccessAnotherClientsWatchlist() throws Exception {
        mockMvc.perform(get("/api/watchlists/{clientId}", OTHER_CLIENT_ID)
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/watchlists")
                        .with(jwtWithRole("CLIENT"))
                        .contentType("application/json")
                        .content("{\"clientId\":\"55555555-5555-4555-8555-555555555555\",\"instrumentId\":\"22222222-2222-4222-8222-222222222222\"}"))
                .andExpect(status().isForbidden());

        verify(watchlistService, never()).findByClientId(OTHER_CLIENT_ID);
    }

        @Test
        void clientCannotAccessAdminReadApi() throws Exception {
                mockMvc.perform(get("/api/v1/admin/clients")
                                                .with(jwtWithRole("CLIENT")))
                                .andExpect(status().isForbidden());
        }

    @Test
    void pendingOrderReadRequiresAccountScope() throws Exception {
        mockMvc.perform(get("/api/v1/orders/pending")
                        .with(jwtWithRole("CLIENT")))
                .andExpect(status().isBadRequest());

        verify(orderService, never()).getPendingOrders(org.mockito.ArgumentMatchers.any());
    }

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor jwtWithRole(String role) {
        return SecurityMockMvcRequestPostProcessors.jwt()
                                .jwt(jwt -> jwt.subject(CLIENT_ID.toString())
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
        @Import({JwtSecurityConfig.class, AccountController.class, AdminController.class,
                AdminReadController.class, OrderController.class, WatchlistController.class})
    static class TestApplication {
        @Bean AccountService accountService() { return mock(AccountService.class); }
        @Bean AdminService adminService() { return mock(AdminService.class); }
        @Bean OrderService orderService() { return mock(OrderService.class); }
        @Bean TransactionsService transactionsService() { return mock(TransactionsService.class); }
        @Bean WatchlistService watchlistService() { return mock(WatchlistService.class); }
        @Bean AccountRepository accountRepository() { return mock(AccountRepository.class); }
        @Bean OrderRepository orderRepository() { return mock(OrderRepository.class); }
        @Bean MissionAuthorizationService missionAuthorizationService(
                AccountRepository accountRepository, OrderRepository orderRepository) {
            return new MissionAuthorizationService(accountRepository, orderRepository);
        }
    }
}
