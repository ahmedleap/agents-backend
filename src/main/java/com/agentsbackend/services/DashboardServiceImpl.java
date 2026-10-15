package com.agentsbackend.services;

import com.agentsbackend.DTO.response.AccountsResponse;
import com.agentsbackend.DTO.response.DashboardResponse;
import com.agentsbackend.DTO.response.WatchlistResponse;
import com.agentsbackend.entities.Account;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.repos.transactional.AccountRepository;
import com.agentsbackend.repos.transactional.ClientRepository;
import com.agentsbackend.repos.transactional.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private static final int MARKET_MOVER_LIMIT = 5;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final DashboardRepository dashboardRepository;
    private final WatchlistService watchlistService;

    public DashboardServiceImpl(AccountRepository accountRepository,
                                ClientRepository clientRepository,
                                DashboardRepository dashboardRepository,
                                WatchlistService watchlistService) {
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.dashboardRepository = dashboardRepository;
        this.watchlistService = watchlistService;
    }

    @Override
    public DashboardResponse getDashboard(UUID clientId, String period) {
        if (clientId == null) {
            throw new IllegalArgumentException("clientId is required");
        }
        clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));

        int days = parsePeriod(period);
        List<AccountsResponse.AccountListItem> accounts = accountRepository.findByClientId(clientId).stream()
                .filter(account -> account.getStatus() == AccountStatus.ACTIVE)
                .map(this::toAccountSummary)
                .toList();

        DashboardResponse response = new DashboardResponse(
                accounts,
                dashboardRepository.findMarketMovers(MARKET_MOVER_LIMIT),
                watchlistService.findByClientId(clientId),
                dashboardRepository.findRecentTrades(clientId),
                dashboardRepository.findPortfolioHistory(clientId, LocalDate.now().minusDays(days - 1)));

        BigDecimal currentValue = accounts.stream()
            .map(AccountsResponse.AccountListItem::getTotalValue)
            .map(this::defaultZero)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal previousValue = defaultZero(
            dashboardRepository.findPreviousPortfolioValue(clientId, LocalDate.now()));
        BigDecimal todayProfitLoss = currentValue.subtract(previousValue);
        BigDecimal todayProfitLossPercent = previousValue.signum() == 0
            ? BigDecimal.ZERO
            : todayProfitLoss.multiply(BigDecimal.valueOf(100))
            .divide(previousValue, 4, java.math.RoundingMode.HALF_UP);
        response.setTodayProfitLoss(todayProfitLoss);
        response.setTodayProfitLossPercent(todayProfitLossPercent);
        return response;
    }

    private AccountsResponse.AccountListItem toAccountSummary(Account account) {
        BigDecimal holdingsValue = defaultZero(accountRepository.getPortfolioValue(account.getAccountId()));
        BigDecimal reservedFunds = defaultZero(accountRepository.getReservedFundsForOpenOrders(account.getAccountId()));
        BigDecimal cashBalance = defaultZero(account.getCashBalance());
        return new AccountsResponse.AccountListItem(
                account.getAccountId(), account.getName(), account.getStatus(), cashBalance,
                cashBalance.subtract(reservedFunds), cashBalance.add(holdingsValue));
    }

    private int parsePeriod(String period) {
        String value = period == null ? "30D" : period.trim().toUpperCase();
        if (!value.endsWith("D")) {
            throw new IllegalArgumentException("Period must be expressed as nD, up to 365D");
        }
        try {
            int days = Integer.parseInt(value.substring(0, value.length() - 1));
            if (days < 1 || days > 365) {
                throw new IllegalArgumentException("Period must be between 1D and 365D");
            }
            return days;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Period must be expressed as nD, up to 365D", exception);
        }
    }

    private BigDecimal defaultZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}