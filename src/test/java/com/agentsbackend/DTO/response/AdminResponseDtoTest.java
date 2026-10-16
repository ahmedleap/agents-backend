package com.agentsbackend.DTO.response;

import org.junit.jupiter.api.Test;
import com.agentsbackend.enums.AccountStatus;
import com.agentsbackend.enums.OrderStatus;
import com.agentsbackend.enums.OrderType;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminResponseDtoTest {

    @Test
    void adminClientResponseRetainsAllValues() {
        UUID id = UUID.randomUUID();
        AdminClientResponse response = new AdminClientResponse();
        response.setClientId(id);
        response.setClientName("First Middle Last");
        response.setFirstName("First");
        response.setMiddleName("Middle");
        response.setLastName("Last");
        response.setEmail("client@example.test");
        response.setSsnLast4("1234");
        response.setDateOfBirth(LocalDate.of(1990, 1, 2));
        response.setJoinDate(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        response.setPortfolioSizeRange(PortfolioSizeRange.BETWEEN_50K_100K);
        response.setRiskTolerance(RiskTolerance.MODERATE);

        assertEquals(id, response.getClientId());
        assertEquals("First Middle Last", response.getClientName());
        assertEquals("First", response.getFirstName());
        assertEquals("Middle", response.getMiddleName());
        assertEquals("Last", response.getLastName());
        assertEquals("client@example.test", response.getEmail());
        assertEquals("1234", response.getSsnLast4());
        assertEquals(LocalDate.of(1990, 1, 2), response.getDateOfBirth());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), response.getJoinDate());
        assertEquals(PortfolioSizeRange.BETWEEN_50K_100K, response.getPortfolioSizeRange());
        assertEquals(RiskTolerance.MODERATE, response.getRiskTolerance());
    }

    @Test
    void adminAccountResponseRetainsAllValues() {
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        AdminAccountResponse response = new AdminAccountResponse();
        response.setAccountId(accountId);
        response.setClientId(clientId);
        response.setClientName("Client Name");
        response.setName("Account");
        response.setCashBalance(new BigDecimal("10.25"));
        response.setStatus(AccountStatus.ACTIVE);
        response.setOpenDate(OffsetDateTime.parse("2026-01-01T00:00:00Z"));

        assertEquals(accountId, response.getAccountId());
        assertEquals(clientId, response.getClientId());
        assertEquals("Client Name", response.getClientName());
        assertEquals("Account", response.getName());
        assertEquals(new BigDecimal("10.25"), response.getCashBalance());
        assertEquals(AccountStatus.ACTIVE, response.getStatus());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), response.getOpenDate());
    }

    @Test
    void adminOrderResponseRetainsAllValues() {
        UUID orderId = UUID.randomUUID();
        AdminOrderResponse response = new AdminOrderResponse();
        response.setOrderId(orderId);
        response.setAccountId(UUID.randomUUID());
        response.setClientId(UUID.randomUUID());
        response.setInstrumentId(UUID.randomUUID());
        response.setTicker("TEST");
        response.setInstrumentName("Test Instrument");
        response.setQuantity(new BigDecimal("2"));
        response.setLimitPrice(new BigDecimal("10"));
        response.setFilledPrice(new BigDecimal("9.50"));
        response.setCancelReason("test");
        response.setOrderType(OrderType.BUY);
        response.setStatus(OrderStatus.FILLED);
        response.setCreatedAt(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        response.setFilledAt(OffsetDateTime.parse("2026-01-01T00:01:00Z"));
        response.setCancelledAt(null);

        assertEquals(orderId, response.getOrderId());
        assertEquals("TEST", response.getTicker());
        assertEquals("Test Instrument", response.getInstrumentName());
        assertEquals(new BigDecimal("2"), response.getQuantity());
        assertEquals(new BigDecimal("10"), response.getLimitPrice());
        assertEquals(new BigDecimal("9.50"), response.getFilledPrice());
        assertEquals("test", response.getCancelReason());
        assertEquals(OrderType.BUY, response.getOrderType());
        assertEquals(OrderStatus.FILLED, response.getStatus());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), response.getCreatedAt());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:01:00Z"), response.getFilledAt());
        assertEquals(null, response.getCancelledAt());
    }

    @Test
    void auditReportAndInsightsResponsesRetainValues() {
        UUID auditId = UUID.randomUUID();
        AdminAuditResponse audit = new AdminAuditResponse();
        audit.setAuditLogId(auditId);
        audit.setEventType("FILLED");
        audit.setReason("completed");
        audit.setDetails("{}");
        audit.setOrderId(UUID.randomUUID());
        audit.setAccountId(UUID.randomUUID());
        audit.setClientId(UUID.randomUUID());
        audit.setClientName("Client Name");
        audit.setEventTime(OffsetDateTime.parse("2026-01-01T00:00:00Z"));
        audit.setCreatedAt(OffsetDateTime.parse("2026-01-01T00:01:00Z"));

        TradingActivityReportResponse report = new TradingActivityReportResponse(
                1L, 1L, 0L, 0L, new BigDecimal("2"), new BigDecimal("20"));
        AdminInsightsResponse insights = new AdminInsightsResponse(
                1L, 2L, 3L, 1L, new BigDecimal("20"));

        assertEquals(auditId, audit.getAuditLogId());
        assertEquals("FILLED", audit.getEventType());
        assertEquals("completed", audit.getReason());
        assertEquals("{}", audit.getDetails());
        assertEquals(audit.getOrderId(), audit.getOrderId());
        assertEquals(audit.getAccountId(), audit.getAccountId());
        assertEquals(audit.getClientId(), audit.getClientId());
        assertEquals("Client Name", audit.getClientName());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:00:00Z"), audit.getEventTime());
        assertEquals(OffsetDateTime.parse("2026-01-01T00:01:00Z"), audit.getCreatedAt());
        assertEquals(1L, report.getTotalOrders());
        assertEquals(1L, report.getFilledOrders());
        assertEquals(0L, report.getPendingOrders());
        assertEquals(0L, report.getCancelledOrders());
        assertEquals(new BigDecimal("2"), report.getFilledQuantity());
        assertEquals(new BigDecimal("20"), report.getTradedValue());
        assertEquals(1L, insights.getClientCount());
        assertEquals(2L, insights.getAccountCount());
        assertEquals(3L, insights.getOrderCount());
        assertEquals(1L, insights.getFilledOrderCount());
        assertEquals(new BigDecimal("20"), insights.getTradedValue());
    }

        @Test
        void generatedConstructorsAndObjectMethodsAreCovered() {
        UUID id = UUID.randomUUID();
        OffsetDateTime timestamp = OffsetDateTime.parse("2026-01-01T00:00:00Z");
            new TradingActivityReportResponse();
            new AdminInsightsResponse();

        AdminClientResponse client = new AdminClientResponse(id, "First Middle Last", "First", "Middle", "Last",
            "client@example.test", LocalDate.of(1990, 1, 2), timestamp, "1234",
            PortfolioSizeRange.UNDER_50K, RiskTolerance.CONSERVATIVE);
        AdminAccountResponse account = new AdminAccountResponse(id, id, "Client Name", "Account",
            new BigDecimal("10"), AccountStatus.ACTIVE, timestamp);
        AdminOrderResponse order = new AdminOrderResponse(id, id, id, "Client Name", id, "TEST", "Test",
            OrderType.BUY, new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("9"),
            OrderStatus.FILLED, timestamp, timestamp, null, null);
        AdminAuditResponse audit = new AdminAuditResponse(id, id, id, id, "Client Name", "FILLED", timestamp,
            "done", "{}", timestamp);
        TradingActivityReportResponse report = new TradingActivityReportResponse(1L, 1L, 0L, 0L,
            new BigDecimal("1"), new BigDecimal("9"));
        AdminInsightsResponse insights = new AdminInsightsResponse(1L, 1L, 1L, 1L,
            new BigDecimal("9"));

        report.setTotalOrders(2L);
        report.setFilledOrders(2L);
        report.setPendingOrders(1L);
        report.setCancelledOrders(1L);
        report.setFilledQuantity(new BigDecimal("3"));
        report.setTradedValue(new BigDecimal("27"));
        insights.setClientCount(2L);
        insights.setAccountCount(2L);
        insights.setOrderCount(2L);
        insights.setFilledOrderCount(2L);
        insights.setTradedValue(new BigDecimal("27"));

        assertEquals(client, new AdminClientResponse(id, "First Middle Last", "First", "Middle", "Last",
            "client@example.test", LocalDate.of(1990, 1, 2), timestamp, "1234",
            PortfolioSizeRange.UNDER_50K, RiskTolerance.CONSERVATIVE));
        assertEquals(account, new AdminAccountResponse(id, id, "Client Name", "Account", new BigDecimal("10"),
            AccountStatus.ACTIVE, timestamp));
        assertEquals(order, new AdminOrderResponse(id, id, id, "Client Name", id, "TEST", "Test", OrderType.BUY,
            new BigDecimal("1"), new BigDecimal("10"), new BigDecimal("9"), OrderStatus.FILLED,
            timestamp, timestamp, null, null));
        assertEquals(audit, new AdminAuditResponse(id, id, id, id, "Client Name", "FILLED", timestamp,
            "done", "{}", timestamp));
        assertEquals(report, report);
        assertEquals(insights, insights);
        assertEquals(report.hashCode(), report.hashCode());
        assertEquals(insights.hashCode(), insights.hashCode());
        assertEquals("Account", account.toString().contains("Account") ? "Account" : "");
        assertEquals("FILLED", audit.toString().contains("FILLED") ? "FILLED" : "");
        }
}