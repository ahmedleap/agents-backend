package com.agentsbackend.DTO.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import com.agentsbackend.enums.AccountStatus;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Consolidated request DTO container for all account-related API requests.
 * Contains nested classes for different request types to keep DTOs organized.
 */
public class AccountsRequest {

    /**
     * Request for creating a new account.
     */
    public static class CreateAccount {
        @NotNull(message = "Client ID cannot be null")
        private UUID clientId;

        @NotBlank(message = "Account name cannot be blank")
        private String name;

        @NotNull(message = "Initial cash balance cannot be null")
        @Positive(message = "Initial cash balance must be positive")
        private BigDecimal initialCashBalance;

        // Constructors
        public CreateAccount() {}

        public CreateAccount(UUID clientId, String name, BigDecimal initialCashBalance) {
            this.clientId = clientId;
            this.name = name;
            this.initialCashBalance = initialCashBalance;
        }

        // Getters and Setters
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

        public BigDecimal getInitialCashBalance() {
            return initialCashBalance;
        }

        public void setInitialCashBalance(BigDecimal initialCashBalance) {
            this.initialCashBalance = initialCashBalance;
        }

        @Override
        public String toString() {
            return "CreateAccount{" +
                    "clientId=" + clientId +
                    ", name='" + name + '\'' +
                    ", initialCashBalance=" + initialCashBalance +
                    '}';
        }
    }

    /**
     * Request for updating account details.
     */
    public static class UpdateAccount {
        @NotBlank(message = "Account name cannot be blank")
        private String name;

        private AccountStatus status;

        // Constructors
        public UpdateAccount() {}

        public UpdateAccount(String name, AccountStatus status) {
            this.name = name;
            this.status = status;
        }

        // Getters and Setters
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

        @Override
        public String toString() {
            return "UpdateAccount{" +
                    "name='" + name + '\'' +
                    ", status=" + status +
                    '}';
        }
    }

    /**
     * Request for cash deposit transaction.
     */
    public static class Deposit {
        @NotNull(message = "Deposit amount cannot be null")
        @Positive(message = "Deposit amount must be positive")
        private BigDecimal amount;

        // Constructors
        public Deposit() {}

        public Deposit(BigDecimal amount) {
            this.amount = amount;
        }

        // Getters and Setters
        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "Deposit{" +
                    "amount=" + amount +
                    '}';
        }
    }

    /**
     * Request for cash withdrawal transaction.
     */
    public static class Withdrawal {
        @NotNull(message = "Withdrawal amount cannot be null")
        @Positive(message = "Withdrawal amount must be positive")
        private BigDecimal amount;

        // Constructors
        public Withdrawal() {}

        public Withdrawal(BigDecimal amount) {
            this.amount = amount;
        }

        // Getters and Setters
        public BigDecimal getAmount() {
            return amount;
        }

        public void setAmount(BigDecimal amount) {
            this.amount = amount;
        }

        @Override
        public String toString() {
            return "Withdrawal{" +
                    "amount=" + amount +
                    '}';
        }
    }
}
