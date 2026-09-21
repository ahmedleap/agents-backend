package com.agentsbackend.DTO.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.agentsbackend.enums.AccountStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Consolidated response DTO container for all account-related API responses.
 * Contains nested classes for different response types to keep DTOs organized.
 */
public class AccountsResponse {

    /**
     * Response DTO for Account entity - basic account information.
     */
    public static class Account {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID accountId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID clientId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String name;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal cashBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private AccountStatus status;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private LocalDateTime openDate;

        // Constructors
        public Account() {}

        public Account(UUID accountId, UUID clientId, String name, BigDecimal cashBalance,
                      AccountStatus status, LocalDateTime openDate) {
            this.accountId = accountId;
            this.clientId = clientId;
            this.name = name;
            this.cashBalance = cashBalance;
            this.status = status;
            this.openDate = openDate;
        }

        // Getters and Setters
        public UUID getAccountId() {
            return accountId;
        }

        public void setAccountId(UUID accountId) {
            this.accountId = accountId;
        }

        public UUID getClientId() {
            return clientId;
        }

        public void setClientId(UUID clientId) {
            this.clientId = clientId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(BigDecimal cashBalance) {
            this.cashBalance = cashBalance;
        }

        public AccountStatus getStatus() {
            return status;
        }

        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        public LocalDateTime getOpenDate() {
            return openDate;
        }

        public void setOpenDate(LocalDateTime openDate) {
            this.openDate = openDate;
        }

        @Override
        public String toString() {
            return "Account{" +
                    "accountId=" + accountId +
                    ", clientId=" + clientId +
                    ", name='" + name + '\'' +
                    ", cashBalance=" + cashBalance +
                    ", status=" + status +
                    ", openDate=" + openDate +
                    '}';
        }
    }

    /**
     * List view of account - compact format for GET /accounts endpoint.
     * Includes key balance information and portfolio value.
     */
    public static class AccountListItem {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID accountId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String name;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private AccountStatus status;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal cashBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal availableBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal totalValue;

        // Constructors
        public AccountListItem() {}

        public AccountListItem(UUID accountId, String name, AccountStatus status,
                              BigDecimal cashBalance, BigDecimal availableBalance, BigDecimal totalValue) {
            this.accountId = accountId;
            this.name = name;
            this.status = status;
            this.cashBalance = cashBalance;
            this.availableBalance = availableBalance;
            this.totalValue = totalValue;
        }

        // Getters and Setters
        public UUID getAccountId() {
            return accountId;
        }

        public void setAccountId(UUID accountId) {
            this.accountId = accountId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public AccountStatus getStatus() {
            return status;
        }

        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        public BigDecimal getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(BigDecimal cashBalance) {
            this.cashBalance = cashBalance;
        }

        public BigDecimal getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(BigDecimal availableBalance) {
            this.availableBalance = availableBalance;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }

        @Override
        public String toString() {
            return "AccountListItem{" +
                    "accountId=" + accountId +
                    ", name='" + name + '\'' +
                    ", status=" + status +
                    ", cashBalance=" + cashBalance +
                    ", availableBalance=" + availableBalance +
                    ", totalValue=" + totalValue +
                    '}';
        }
    }

    /**
     * Detailed response DTO for Account with related holdings and transactions.
     * Includes counts of related resources.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AccountDetail {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID accountId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private UUID clientId;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String name;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal cashBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal availableBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private AccountStatus status;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private LocalDateTime openDate;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Integer holdingCount;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Integer orderCount;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Integer transactionCount;

        // Constructors
        public AccountDetail() {}

        public AccountDetail(UUID accountId, UUID clientId, String name, BigDecimal cashBalance,
                            BigDecimal availableBalance, AccountStatus status, LocalDateTime openDate,
                            Integer holdingCount, Integer orderCount, Integer transactionCount) {
            this.accountId = accountId;
            this.clientId = clientId;
            this.name = name;
            this.cashBalance = cashBalance;
            this.availableBalance = availableBalance;
            this.status = status;
            this.openDate = openDate;
            this.holdingCount = holdingCount;
            this.orderCount = orderCount;
            this.transactionCount = transactionCount;
        }

        // Getters and Setters
        public UUID getAccountId() {
            return accountId;
        }

        public void setAccountId(UUID accountId) {
            this.accountId = accountId;
        }

        public UUID getClientId() {
            return clientId;
        }

        public void setClientId(UUID clientId) {
            this.clientId = clientId;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigDecimal getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(BigDecimal cashBalance) {
            this.cashBalance = cashBalance;
        }

        public BigDecimal getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(BigDecimal availableBalance) {
            this.availableBalance = availableBalance;
        }

        public AccountStatus getStatus() {
            return status;
        }

        public void setStatus(AccountStatus status) {
            this.status = status;
        }

        public LocalDateTime getOpenDate() {
            return openDate;
        }

        public void setOpenDate(LocalDateTime openDate) {
            this.openDate = openDate;
        }

        public Integer getHoldingCount() {
            return holdingCount;
        }

        public void setHoldingCount(Integer holdingCount) {
            this.holdingCount = holdingCount;
        }

        public Integer getOrderCount() {
            return orderCount;
        }

        public void setOrderCount(Integer orderCount) {
            this.orderCount = orderCount;
        }

        public Integer getTransactionCount() {
            return transactionCount;
        }

        public void setTransactionCount(Integer transactionCount) {
            this.transactionCount = transactionCount;
        }

        @Override
        public String toString() {
            return "AccountDetail{" +
                    "accountId=" + accountId +
                    ", clientId=" + clientId +
                    ", name='" + name + '\'' +
                    ", cashBalance=" + cashBalance +
                    ", availableBalance=" + availableBalance +
                    ", status=" + status +
                    ", openDate=" + openDate +
                    ", holdingCount=" + holdingCount +
                    ", orderCount=" + orderCount +
                    ", transactionCount=" + transactionCount +
                    '}';
        }
    }

    /**
     * Summary response DTO for portfolio valuation.
     * Includes cash, holdings value, and total portfolio value.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Summary {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal cashBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal availableBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal holdingsValue;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal totalValue;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String summaryDate;

        // Constructors
        public Summary() {}

        public Summary(BigDecimal cashBalance, BigDecimal availableBalance,
                      BigDecimal holdingsValue, BigDecimal totalValue, String summaryDate) {
            this.cashBalance = cashBalance;
            this.availableBalance = availableBalance;
            this.holdingsValue = holdingsValue;
            this.totalValue = totalValue;
            this.summaryDate = summaryDate;
        }

        // Getters and Setters
        public BigDecimal getCashBalance() {
            return cashBalance;
        }

        public void setCashBalance(BigDecimal cashBalance) {
            this.cashBalance = cashBalance;
        }

        public BigDecimal getAvailableBalance() {
            return availableBalance;
        }

        public void setAvailableBalance(BigDecimal availableBalance) {
            this.availableBalance = availableBalance;
        }

        public BigDecimal getHoldingsValue() {
            return holdingsValue;
        }

        public void setHoldingsValue(BigDecimal holdingsValue) {
            this.holdingsValue = holdingsValue;
        }

        public BigDecimal getTotalValue() {
            return totalValue;
        }

        public void setTotalValue(BigDecimal totalValue) {
            this.totalValue = totalValue;
        }

        public String getSummaryDate() {
            return summaryDate;
        }

        public void setSummaryDate(String summaryDate) {
            this.summaryDate = summaryDate;
        }

        @Override
        public String toString() {
            return "Summary{" +
                    "cashBalance=" + cashBalance +
                    ", availableBalance=" + availableBalance +
                    ", holdingsValue=" + holdingsValue +
                    ", totalValue=" + totalValue +
                    ", summaryDate='" + summaryDate + '\'' +
                    '}';
        }
    }

    /**
     * Performance response DTO for account performance metrics over a period.
     * Includes returns, gains/losses, and performance statistics.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Performance {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String period;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal startingValue;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal endingValue;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal totalReturn;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal returnPercentage;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal realizedGainLoss;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal unrealizedGainLoss;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String periodStartDate;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String periodEndDate;

        // Constructors
        public Performance() {}

        public Performance(String period, BigDecimal startingValue, BigDecimal endingValue,
                          BigDecimal totalReturn, BigDecimal returnPercentage,
                          BigDecimal realizedGainLoss, BigDecimal unrealizedGainLoss,
                          String periodStartDate, String periodEndDate) {
            this.period = period;
            this.startingValue = startingValue;
            this.endingValue = endingValue;
            this.totalReturn = totalReturn;
            this.returnPercentage = returnPercentage;
            this.realizedGainLoss = realizedGainLoss;
            this.unrealizedGainLoss = unrealizedGainLoss;
            this.periodStartDate = periodStartDate;
            this.periodEndDate = periodEndDate;
        }

        // Getters and Setters
        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public BigDecimal getStartingValue() {
            return startingValue;
        }

        public void setStartingValue(BigDecimal startingValue) {
            this.startingValue = startingValue;
        }

        public BigDecimal getEndingValue() {
            return endingValue;
        }

        public void setEndingValue(BigDecimal endingValue) {
            this.endingValue = endingValue;
        }

        public BigDecimal getTotalReturn() {
            return totalReturn;
        }

        public void setTotalReturn(BigDecimal totalReturn) {
            this.totalReturn = totalReturn;
        }

        public BigDecimal getReturnPercentage() {
            return returnPercentage;
        }

        public void setReturnPercentage(BigDecimal returnPercentage) {
            this.returnPercentage = returnPercentage;
        }

        public BigDecimal getRealizedGainLoss() {
            return realizedGainLoss;
        }

        public void setRealizedGainLoss(BigDecimal realizedGainLoss) {
            this.realizedGainLoss = realizedGainLoss;
        }

        public BigDecimal getUnrealizedGainLoss() {
            return unrealizedGainLoss;
        }

        public void setUnrealizedGainLoss(BigDecimal unrealizedGainLoss) {
            this.unrealizedGainLoss = unrealizedGainLoss;
        }

        public String getPeriodStartDate() {
            return periodStartDate;
        }

        public void setPeriodStartDate(String periodStartDate) {
            this.periodStartDate = periodStartDate;
        }

        public String getPeriodEndDate() {
            return periodEndDate;
        }

        public void setPeriodEndDate(String periodEndDate) {
            this.periodEndDate = periodEndDate;
        }

        @Override
        public String toString() {
            return "Performance{" +
                    "period='" + period + '\'' +
                    ", startingValue=" + startingValue +
                    ", endingValue=" + endingValue +
                    ", totalReturn=" + totalReturn +
                    ", returnPercentage=" + returnPercentage +
                    ", realizedGainLoss=" + realizedGainLoss +
                    ", unrealizedGainLoss=" + unrealizedGainLoss +
                    ", periodStartDate='" + periodStartDate + '\'' +
                    ", periodEndDate='" + periodEndDate + '\'' +
                    '}';
        }
    }

    /**
     * Response DTO for cash transaction operations (deposit/withdrawal).
     * Provides feedback on transaction results.
     */
    public static class Transaction {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String message;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private BigDecimal newCashBalance;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private String transactionType;

        // Constructors
        public Transaction() {}

        public Transaction(String message, BigDecimal newCashBalance, String transactionType) {
            this.message = message;
            this.newCashBalance = newCashBalance;
            this.transactionType = transactionType;
        }

        // Getters and Setters
        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public BigDecimal getNewCashBalance() {
            return newCashBalance;
        }

        public void setNewCashBalance(BigDecimal newCashBalance) {
            this.newCashBalance = newCashBalance;
        }

        public String getTransactionType() {
            return transactionType;
        }

        public void setTransactionType(String transactionType) {
            this.transactionType = transactionType;
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "message='" + message + '\'' +
                    ", newCashBalance=" + newCashBalance +
                    ", transactionType='" + transactionType + '\'' +
                    '}';
        }
    }
}
