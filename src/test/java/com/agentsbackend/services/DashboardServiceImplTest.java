package com.agentsbackend.services;

import com.agentsbackend.DTO.response.DashboardResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.repos.AccountRepository;
import com.agentsbackend.repos.ClientRepository;
import com.agentsbackend.repos.DashboardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private DashboardRepository dashboardRepository;
    @Mock
    private WatchlistService watchlistService;

    private DashboardServiceImpl dashboardService;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        dashboardService = new DashboardServiceImpl(
                accountRepository, clientRepository, dashboardRepository, watchlistService);
        clientId = UUID.randomUUID();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(new Client()));
    }

    @Test
    void includesOnlyActiveAccountsAndCalculatesAccountValues() {
        Account active = account(AccountStatus.ACTIVE, "Active", "100.00");
        Account closed = account(AccountStatus.CLOSED, "Closed", "999.00");
        when(dashboardRepository.findMarketMovers(5)).thenReturn(List.of());
        when(dashboardRepository.findRecentTrades(clientId)).thenReturn(List.of());
        when(dashboardRepository.findPortfolioHistory(any(), any())).thenReturn(List.of());
        when(watchlistService.findByClientId(clientId)).thenReturn(List.of());
        when(accountRepository.findByClientId(clientId)).thenReturn(List.of(active, closed));
        when(accountRepository.getPortfolioValue(active.getAccountId())).thenReturn(new BigDecimal("50.00"));
        when(accountRepository.getReservedFundsForOpenOrders(active.getAccountId())).thenReturn(new BigDecimal("10.00"));

        DashboardResponse response = dashboardService.getDashboard(clientId, "90D");

        assertEquals(1, response.getAccounts().size());
        assertEquals(new BigDecimal("150.00"), response.getAccounts().get(0).getTotalValue());
        assertEquals(new BigDecimal("90.00"), response.getAccounts().get(0).getAvailableBalance());
        verify(dashboardRepository).findPortfolioHistory(any(), any());
    }

    @Test
    void rejectsPeriodsLongerThanOneYear() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getDashboard(clientId, "366D"));
    }

    private Account account(AccountStatus status, String name, String cash) {
        Account account = new Account();
        account.setAccountId(UUID.randomUUID());
        account.setClientId(clientId);
        account.setName(name);
        account.setCashBalance(new BigDecimal(cash));
        account.setStatus(status);
        return account;
    }
}