package com.agentsbackend.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler();
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/orders");
    }

    // ==================== handleInvalidOrderParameters Tests ====================

    @Test
    @DisplayName("Should handle InvalidOrderParametersException")
    void testHandleInvalidOrderParameters() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Invalid quantity");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("InvalidOrderParametersException", response.getBody().get("error"));
        assertEquals("Invalid quantity", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should include orderId when present in InvalidOrderParametersException")
    void testHandleInvalidOrderParametersWithOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Invalid order", orderId);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("orderId"));
        assertEquals(orderId.toString(), response.getBody().get("orderId").toString());
    }

    @Test
    @DisplayName("Should not include orderId when null in InvalidOrderParametersException")
    void testHandleInvalidOrderParametersWithoutOrderId() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Invalid order");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertFalse(response.getBody().containsKey("orderId"));
    }

    @Test
    @DisplayName("Should include timestamp in InvalidOrderParametersException response")
    void testHandleInvalidOrderParametersIncludesTimestamp() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");
        LocalDateTime before = LocalDateTime.now();

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);
        LocalDateTime after = LocalDateTime.now();

        // Assert
        assertTrue(response.getBody().containsKey("timestamp"));
        assertNotNull(response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Should include path in InvalidOrderParametersException response")
    void testHandleInvalidOrderParametersIncludesPath() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("path"));
        assertEquals("/api/v1/orders", response.getBody().get("path"));
    }

    // ==================== handleOrderNotFound Tests ====================

    @Test
    @DisplayName("Should handle OrderNotFoundException")
    void testHandleOrderNotFound() {
        // Arrange
        OrderNotFoundException exception = new OrderNotFoundException("Order not found");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleOrderNotFound(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().get("status"));
        assertEquals("OrderNotFoundException", response.getBody().get("error"));
        assertEquals("Order not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should include timestamp in OrderNotFoundException response")
    void testHandleOrderNotFoundIncludesTimestamp() {
        // Arrange
        OrderNotFoundException exception = new OrderNotFoundException("Not found");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleOrderNotFound(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should include path in OrderNotFoundException response")
    void testHandleOrderNotFoundIncludesPath() {
        // Arrange
        OrderNotFoundException exception = new OrderNotFoundException("Not found");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleOrderNotFound(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("path"));
        assertEquals("/api/v1/orders", response.getBody().get("path"));
    }

    // ==================== handleInvalidOrderStatus Tests ====================

    @Test
    @DisplayName("Should handle InvalidOrderStatusException")
    void testHandleInvalidOrderStatus() {
        // Arrange
        InvalidOrderStatusException exception = new InvalidOrderStatusException("Cannot cancel filled order");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderStatus(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("InvalidOrderStatusException", response.getBody().get("error"));
        assertEquals("Cannot cancel filled order", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should include timestamp in InvalidOrderStatusException response")
    void testHandleInvalidOrderStatusIncludesTimestamp() {
        // Arrange
        InvalidOrderStatusException exception = new InvalidOrderStatusException("Invalid status");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderStatus(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should include path in InvalidOrderStatusException response")
    void testHandleInvalidOrderStatusIncludesPath() {
        // Arrange
        InvalidOrderStatusException exception = new InvalidOrderStatusException("Invalid status");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderStatus(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("path"));
        assertEquals("/api/v1/orders", response.getBody().get("path"));
    }

    // ==================== handleInsufficientFunds Tests ====================

    @Test
    @DisplayName("Should handle InsufficientFundsException")
    void testHandleInsufficientFunds() {
        // Arrange
        InsufficientFundsException exception = new InsufficientFundsException("Not enough cash");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("InsufficientFundsException", response.getBody().get("error"));
        assertEquals("Not enough cash", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should include orderId when present in InsufficientFundsException")
    void testHandleInsufficientFundsWithOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        InsufficientFundsException exception = new InsufficientFundsException("Not enough funds", orderId);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("orderId"));
        assertEquals(orderId.toString(), response.getBody().get("orderId").toString());
    }

    @Test
    @DisplayName("Should not include orderId when null in InsufficientFundsException")
    void testHandleInsufficientFundsWithoutOrderId() {
        // Arrange
        InsufficientFundsException exception = new InsufficientFundsException("Not enough funds");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Assert
        assertFalse(response.getBody().containsKey("orderId"));
    }

    @Test
    @DisplayName("Should include timestamp in InsufficientFundsException response")
    void testHandleInsufficientFundsIncludesTimestamp() {
        // Arrange
        InsufficientFundsException exception = new InsufficientFundsException("No funds");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should include path in InsufficientFundsException response")
    void testHandleInsufficientFundsIncludesPath() {
        // Arrange
        InsufficientFundsException exception = new InsufficientFundsException("No funds");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInsufficientFunds(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("path"));
        assertEquals("/api/v1/orders", response.getBody().get("path"));
    }

    // ==================== handleInvalidAccount Tests ====================

    @Test
    @DisplayName("Should handle InvalidAccountException")
    void testHandleInvalidAccount() {
        // Arrange
        InvalidAccountException exception = new InvalidAccountException("Account not found");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidAccount(exception, webRequest);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().get("status"));
        assertEquals("InvalidAccountException", response.getBody().get("error"));
        assertEquals("Account not found", response.getBody().get("message"));
    }

    @Test
    @DisplayName("Should include orderId when present in InvalidAccountException")
    void testHandleInvalidAccountWithOrderId() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        InvalidAccountException exception = new InvalidAccountException("Account error", orderId);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidAccount(exception, webRequest);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().containsKey("orderId"));
        assertEquals(orderId.toString(), response.getBody().get("orderId").toString());
    }

    @Test
    @DisplayName("Should not include orderId when null in InvalidAccountException")
    void testHandleInvalidAccountWithoutOrderId() {
        // Arrange
        InvalidAccountException exception = new InvalidAccountException("Account error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidAccount(exception, webRequest);

        // Assert
        assertFalse(response.getBody().containsKey("orderId"));
    }

    @Test
    @DisplayName("Should include timestamp in InvalidAccountException response")
    void testHandleInvalidAccountIncludesTimestamp() {
        // Arrange
        InvalidAccountException exception = new InvalidAccountException("Account error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidAccount(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("timestamp"));
    }

    @Test
    @DisplayName("Should include path in InvalidAccountException response")
    void testHandleInvalidAccountIncludesPath() {
        // Arrange
        InvalidAccountException exception = new InvalidAccountException("Account error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidAccount(exception, webRequest);

        // Assert
        assertTrue(response.getBody().containsKey("path"));
        assertEquals("/api/v1/orders", response.getBody().get("path"));
    }

    // ==================== Response Structure Tests ====================

    @Test
    @DisplayName("All exceptions should return 400 status except OrderNotFound")
    void testHttpStatusCodes() {
        // Arrange
        InvalidOrderParametersException paramEx = new InvalidOrderParametersException("Error");
        OrderNotFoundException notFoundEx = new OrderNotFoundException("Not found");
        InvalidOrderStatusException statusEx = new InvalidOrderStatusException("Error");
        InsufficientFundsException fundsEx = new InsufficientFundsException("Error");
        InvalidAccountException accountEx = new InvalidAccountException("Error");

        // Act & Assert
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleInvalidOrderParameters(paramEx, webRequest).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, exceptionHandler.handleOrderNotFound(notFoundEx, webRequest).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleInvalidOrderStatus(statusEx, webRequest).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleInsufficientFunds(fundsEx, webRequest).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, exceptionHandler.handleInvalidAccount(accountEx, webRequest).getStatusCode());
    }

    @Test
    @DisplayName("All responses should have required fields")
    void testResponseHasRequiredFields() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);
        Map<String, Object> body = response.getBody();

        // Assert
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.containsKey("status"));
        assertTrue(body.containsKey("error"));
        assertTrue(body.containsKey("message"));
        assertTrue(body.containsKey("path"));
    }

    @Test
    @DisplayName("Message should be preserved in error response")
    void testMessagePreservation() {
        // Arrange
        String errorMessage = "This is a detailed error message with specific information";
        InvalidOrderParametersException exception = new InvalidOrderParametersException(errorMessage);

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertEquals(errorMessage, response.getBody().get("message"));
    }

    @Test
    @DisplayName("Different paths should be handled correctly")
    void testDifferentPaths() {
        // Arrange
        when(webRequest.getDescription(false)).thenReturn("uri=/api/v1/accounts");
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Act
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleInvalidOrderParameters(exception, webRequest);

        // Assert
        assertEquals("/api/v1/accounts", response.getBody().get("path"));
    }
}
