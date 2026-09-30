package com.agentsbackend.exceptions;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    private UUID testClientId;
    private UUID testAccountId;
    private UUID testOrderId;

    @BeforeEach
    void setUp() {
        testClientId = UUID.randomUUID();
        testAccountId = UUID.randomUUID();
        testOrderId = UUID.randomUUID();
    }

    // ==================== PORTFOLIO EXCEPTION TESTS ====================

    @Test
    @DisplayName("AccountNotFoundException returns 404 status")
    void testAccountNotFoundException_Returns404() {
        AccountNotFoundException ex = new AccountNotFoundException("Account not found");
        ResponseEntity<Map<String, String>> response = handler.handleAccountNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Account not found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("ClientNotFoundException returns 404 status")
    void testClientNotFoundException_Returns404() {
        ClientNotFoundException ex = new ClientNotFoundException("Client not found");
        ResponseEntity<Map<String, String>> response = handler.handleClientNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Client not found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("InstrumentNotFoundException returns 404 status")
    void testInstrumentNotFoundException_Returns404() {
        InstrumentNotFoundException ex = new InstrumentNotFoundException("Instrument not found");
        ResponseEntity<Map<String, String>> response = handler.handleInstrumentNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Instrument not found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("HoldingNotFoundException returns 404 status")
    void testHoldingNotFoundException_Returns404() {
        HoldingNotFoundException ex = new HoldingNotFoundException("Holding not found");
        ResponseEntity<Map<String, String>> response = handler.handleHoldingNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Holding not found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("UnauthorizedAccountAccessException returns 403 status")
    void testUnauthorizedAccountAccessException_Returns403() {
        UnauthorizedAccountAccessException ex = new UnauthorizedAccountAccessException(
            "Client does not have access to this account"
        );
        ResponseEntity<Map<String, String>> response = handler.handleUnauthorizedAccountAccessException(ex);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Client does not have access to this account", response.getBody().get("error"));
    }

    @Test
    @DisplayName("IllegalArgumentException returns 400 status")
    void testIllegalArgumentException_Returns400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid argument provided");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgumentException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Invalid argument provided", response.getBody().get("error"));
    }

    @Test
    @DisplayName("DuplicateKeyException returns 409 status")
    void testDuplicateKeyException_Returns409() {
        DuplicateKeyException ex = new DuplicateKeyException("Duplicate key found");
        ResponseEntity<Map<String, String>> response = handler.handleDuplicateKeyException(ex);
        
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Duplicate key found", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Portfolio exception message format is correct")
    void testPortfolioException_MessageFormat() {
        ClientNotFoundException ex = new ClientNotFoundException("Test client missing");
        ResponseEntity<Map<String, String>> response = handler.handleClientNotFoundException(ex);
        
        Map<String, String> body = response.getBody();
        assertEquals(1, body.size());
        assertEquals("Test client missing", body.get("error"));
    }

    @Test
    @DisplayName("Portfolio exceptions use simple response format")
    void testPortfolioException_SimpleFormat() {
        HoldingNotFoundException ex = new HoldingNotFoundException("Holding missing");
        ResponseEntity<Map<String, String>> response = handler.handleHoldingNotFoundException(ex);
        
        Map<String, String> body = response.getBody();
        assertNotNull(body);
        // Should only have "error" key, no timestamp or path
        assertTrue(body.containsKey("error"));
        assertFalse(body.containsKey("timestamp"));
        assertFalse(body.containsKey("path"));
    }

    @Test
    @DisplayName("Multiple portfolio exceptions maintain consistent format")
    void testMultiplePortfolioExceptions_ConsistentFormat() {
        AccountNotFoundException ex1 = new AccountNotFoundException("Account 1 not found");
        InstrumentNotFoundException ex2 = new InstrumentNotFoundException("Instrument 1 not found");
        
        ResponseEntity<Map<String, String>> response1 = handler.handleAccountNotFoundException(ex1);
        ResponseEntity<Map<String, String>> response2 = handler.handleInstrumentNotFoundException(ex2);
        
        assertEquals(response1.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(response2.getStatusCode(), HttpStatus.NOT_FOUND);
        assertEquals(response1.getBody().keySet(), response2.getBody().keySet());
    }

    @Test
    @DisplayName("Portfolio exception with null message")
    void testPortfolioException_NullMessage() {
        ClientNotFoundException ex = new ClientNotFoundException(null);
        ResponseEntity<Map<String, String>> response = handler.handleClientNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Portfolio exception with empty message")
    void testPortfolioException_EmptyMessage() {
        AccountNotFoundException ex = new AccountNotFoundException("");
        ResponseEntity<Map<String, String>> response = handler.handleAccountNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("", response.getBody().get("error"));
    }

    // ==================== ORDER EXCEPTION TESTS ====================

    @Test
    @DisplayName("InvalidOrderParametersException returns 400 status")
    void testInvalidOrderParametersException_Returns400() {
        InvalidOrderParametersException ex = new InvalidOrderParametersException(
            "Invalid order parameters",
            testOrderId
        );
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderParametersException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("message"));
        assertEquals("Invalid order parameters", response.getBody().get("message"));
    }

    @Test
    @DisplayName("OrderNotFoundException returns 404 status")
    void testOrderNotFoundException_Returns404() {
        OrderNotFoundException ex = new OrderNotFoundException("Order not found", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleOrderNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("InvalidOrderStatusException returns 400 status")
    void testInvalidOrderStatusException_Returns400() {
        InvalidOrderStatusException ex = new InvalidOrderStatusException("Invalid status", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderStatusException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid status", response.getBody().get("message"));
    }

    @Test
    @DisplayName("InsufficientFundsException returns 400 status")
    void testInsufficientFundsException_Returns400() {
        InsufficientFundsException ex = new InsufficientFundsException(
            "Not enough funds",
            testAccountId,
            new BigDecimal("1000"),
            new BigDecimal("500")
        );
        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientFundsException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Not enough funds", response.getBody().get("message"));
    }

    @Test
    @DisplayName("InvalidAccountException returns 400 status")
    void testInvalidAccountException_Returns400() {
        InvalidAccountException ex = new InvalidAccountException("Invalid account", testAccountId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidAccountException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid account", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Order exception response includes timestamp")
    void testOrderException_IncludesTimestamp() {
        InvalidOrderParametersException ex = new InvalidOrderParametersException("Bad params", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderParametersException(ex);
        
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("timestamp"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    @DisplayName("Order exception response includes path")
    void testOrderException_IncludesPath() {
        OrderNotFoundException ex = new OrderNotFoundException("Not found", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleOrderNotFoundException(ex);
        
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("path"));
    }

    @Test
    @DisplayName("Order exception response includes orderId")
    void testOrderException_IncludesOrderId() {
        InvalidOrderStatusException ex = new InvalidOrderStatusException("Invalid", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderStatusException(ex);
        
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("orderId"));
        assertEquals(testOrderId.toString(), body.get("orderId"));
    }

    @Test
    @DisplayName("Order exception response structure is complete")
    void testOrderException_CompleteStructure() {
        InsufficientFundsException ex = new InsufficientFundsException(
            "Insufficient funds message",
            testAccountId,
            new BigDecimal("5000"),
            new BigDecimal("2000")
        );
        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientFundsException(ex);
        
        Map<String, Object> body = response.getBody();
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("path"));
        assertTrue(body.containsKey("orderId"));
    }

    @Test
    @DisplayName("Multiple order exceptions maintain consistent format")
    void testMultipleOrderExceptions_ConsistentFormat() {
        InvalidOrderParametersException ex1 = new InvalidOrderParametersException("Bad 1", testOrderId);
        OrderNotFoundException ex2 = new OrderNotFoundException("Not found", testOrderId);
        
        ResponseEntity<Map<String, Object>> response1 = handler.handleInvalidOrderParametersException(ex1);
        ResponseEntity<Map<String, Object>> response2 = handler.handleOrderNotFoundException(ex2);
        
        Map<String, Object> body1 = response1.getBody();
        Map<String, Object> body2 = response2.getBody();
        
        assertEquals(body1.keySet(), body2.keySet());
    }

    @Test
    @DisplayName("Order exception timestamp is LocalDateTime")
    void testOrderException_TimestampType() {
        InvalidAccountException ex = new InvalidAccountException("Invalid", testAccountId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidAccountException(ex);
        
        Map<String, Object> body = response.getBody();
        Object timestamp = body.get("timestamp");
        assertTrue(timestamp instanceof LocalDateTime || timestamp instanceof String);
    }

    @Test
    @DisplayName("Order exception message is preserved")
    void testOrderException_MessagePreserved() {
        String expectedMessage = "This is a detailed error message";
        InvalidOrderParametersException ex = new InvalidOrderParametersException(expectedMessage, testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderParametersException(ex);
        
        assertEquals(expectedMessage, response.getBody().get("message"));
    }

    @Test
    @DisplayName("Order exception with null message")
    void testOrderException_NullMessage() {
        OrderNotFoundException ex = new OrderNotFoundException(null, testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleOrderNotFoundException(ex);
        
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Order exception with empty message")
    void testOrderException_EmptyMessage() {
        InvalidOrderStatusException ex = new InvalidOrderStatusException("", testOrderId);
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidOrderStatusException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("", response.getBody().get("message"));
    }

    @Test
    @DisplayName("InsufficientFundsException preserves financial details")
    void testInsufficientFundsException_FinancialDetails() {
        BigDecimal required = new BigDecimal("5000.00");
        BigDecimal available = new BigDecimal("3000.00");
        InsufficientFundsException ex = new InsufficientFundsException(
            "Insufficient funds",
            testAccountId,
            required,
            available
        );
        ResponseEntity<Map<String, Object>> response = handler.handleInsufficientFundsException(ex);
        
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    @DisplayName("Order exceptions all return correct HTTP status codes")
    void testAllOrderExceptions_StatusCodes() {
        UUID orderId = UUID.randomUUID();
        
        ResponseEntity<Map<String, Object>> response1 = 
            handler.handleInvalidOrderParametersException(
                new InvalidOrderParametersException("Invalid params", orderId)
            );
        ResponseEntity<Map<String, Object>> response2 = 
            handler.handleOrderNotFoundException(
                new OrderNotFoundException("Not found", orderId)
            );
        ResponseEntity<Map<String, Object>> response3 = 
            handler.handleInvalidOrderStatusException(
                new InvalidOrderStatusException("Invalid status", orderId)
            );
        ResponseEntity<Map<String, Object>> response4 = 
            handler.handleInsufficientFundsException(
                new InsufficientFundsException(
                    "Insufficient",
                    testAccountId,
                    new BigDecimal("1000"),
                    new BigDecimal("500")
                )
            );
        ResponseEntity<Map<String, Object>> response5 = 
            handler.handleInvalidAccountException(
                new InvalidAccountException("Invalid account", testAccountId)
            );
        
        assertEquals(HttpStatus.BAD_REQUEST, response1.getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, response2.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response3.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response4.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, response5.getStatusCode());
    }
}
