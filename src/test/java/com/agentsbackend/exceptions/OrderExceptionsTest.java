package com.agentsbackend.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Order Exceptions Tests")
class OrderExceptionsTest {

    // ==================== OrderNotFoundException Tests ====================

    @Test
    @DisplayName("Should create OrderNotFoundException with message")
    void testOrderNotFoundException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String message = "Order not found: " + orderId;

        // Act
        OrderNotFoundException exception = new OrderNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create OrderNotFoundException with message and cause")
    void testOrderNotFoundExceptionWithCause() {
        // Arrange
        String message = "Order not found";
        Throwable cause = new RuntimeException("Database error");

        // Act
        OrderNotFoundException exception = new OrderNotFoundException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("OrderNotFoundException should be RuntimeException")
    void testOrderNotFoundExceptionIsRuntimeException() {
        // Act
        OrderNotFoundException exception = new OrderNotFoundException("Order error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== InvalidOrderParametersException Tests ====================

    @Test
    @DisplayName("Should create InvalidOrderParametersException with message")
    void testInvalidOrderParametersException() {
        // Arrange
        String message = "Invalid order parameters: quantity must be positive";

        // Act
        InvalidOrderParametersException exception = new InvalidOrderParametersException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getOrderId());
    }

    @Test
    @DisplayName("Should create InvalidOrderParametersException with message and orderId")
    void testInvalidOrderParametersExceptionWithOrderId() {
        // Arrange
        String message = "Invalid order parameters";
        UUID orderId = UUID.randomUUID();

        // Act
        InvalidOrderParametersException exception = new InvalidOrderParametersException(message, orderId);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(orderId, exception.getOrderId());
    }

    @Test
    @DisplayName("Should create InvalidOrderParametersException with message and cause")
    void testInvalidOrderParametersExceptionWithCause() {
        // Arrange
        String message = "Invalid parameters";
        Throwable cause = new IllegalArgumentException("Value error");

        // Act
        InvalidOrderParametersException exception = new InvalidOrderParametersException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getOrderId());
    }

    @Test
    @DisplayName("Should set and get orderId for InvalidOrderParametersException")
    void testInvalidOrderParametersExceptionOrderIdSetter() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");
        UUID orderId = UUID.randomUUID();

        // Act
        exception.setOrderId(orderId);

        // Assert
        assertEquals(orderId, exception.getOrderId());
    }

    @Test
    @DisplayName("Should update orderId for InvalidOrderParametersException")
    void testInvalidOrderParametersExceptionUpdateOrderId() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error", orderId1);

        // Act
        exception.setOrderId(orderId2);

        // Assert
        assertEquals(orderId2, exception.getOrderId());
        assertNotEquals(orderId1, exception.getOrderId());
    }

    @Test
    @DisplayName("InvalidOrderParametersException should be RuntimeException")
    void testInvalidOrderParametersExceptionIsRuntimeException() {
        // Act
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== InvalidOrderStatusException Tests ====================

    @Test
    @DisplayName("Should create InvalidOrderStatusException with message")
    void testInvalidOrderStatusException() {
        // Arrange
        String message = "Cannot cancel an already executed order";

        // Act
        InvalidOrderStatusException exception = new InvalidOrderStatusException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create InvalidOrderStatusException with message and cause")
    void testInvalidOrderStatusExceptionWithCause() {
        // Arrange
        String message = "Invalid status";
        Throwable cause = new StateException("State error");

        // Act
        InvalidOrderStatusException exception = new InvalidOrderStatusException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("InvalidOrderStatusException should be RuntimeException")
    void testInvalidOrderStatusExceptionIsRuntimeException() {
        // Act
        InvalidOrderStatusException exception = new InvalidOrderStatusException("Status error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== InsufficientFundsException Tests ====================

    @Test
    @DisplayName("Should create InsufficientFundsException with message")
    void testInsufficientFundsException() {
        // Arrange
        String message = "Insufficient funds for this order";

        // Act
        InsufficientFundsException exception = new InsufficientFundsException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getOrderId());
    }

    @Test
    @DisplayName("Should create InsufficientFundsException with message and orderId")
    void testInsufficientFundsExceptionWithOrderId() {
        // Arrange
        String message = "Insufficient funds";
        UUID orderId = UUID.randomUUID();

        // Act
        InsufficientFundsException exception = new InsufficientFundsException(message, orderId);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(orderId, exception.getOrderId());
    }

    @Test
    @DisplayName("Should create InsufficientFundsException with message and cause")
    void testInsufficientFundsExceptionWithCause() {
        // Arrange
        String message = "Insufficient funds";
        Throwable cause = new ArithmeticException("Calculation error");

        // Act
        InsufficientFundsException exception = new InsufficientFundsException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getOrderId());
    }

    @Test
    @DisplayName("Should set and get orderId for InsufficientFundsException")
    void testInsufficientFundsExceptionOrderIdSetter() {
        // Arrange
        InsufficientFundsException exception = new InsufficientFundsException("Not enough funds");
        UUID orderId = UUID.randomUUID();

        // Act
        exception.setOrderId(orderId);

        // Assert
        assertEquals(orderId, exception.getOrderId());
    }

    @Test
    @DisplayName("InsufficientFundsException should be RuntimeException")
    void testInsufficientFundsExceptionIsRuntimeException() {
        // Act
        InsufficientFundsException exception = new InsufficientFundsException("Funds error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== InvalidAccountException Tests ====================

    @Test
    @DisplayName("Should create InvalidAccountException with message")
    void testInvalidAccountException() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        String message = "Account not found: " + accountId;

        // Act
        InvalidAccountException exception = new InvalidAccountException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create InvalidAccountException with message and cause")
    void testInvalidAccountExceptionWithCause() {
        // Arrange
        String message = "Account error";
        Throwable cause = new RuntimeException("Backend error");

        // Act
        InvalidAccountException exception = new InvalidAccountException(message, cause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("InvalidAccountException should be RuntimeException")
    void testInvalidAccountExceptionIsRuntimeException() {
        // Act
        InvalidAccountException exception = new InvalidAccountException("Account error");

        // Assert
        assertTrue(exception instanceof RuntimeException);
    }

    // ==================== Exception Hierarchy Tests ====================

    @Test
    @DisplayName("All order exceptions should extend RuntimeException")
    void testAllExceptionsExtendRuntimeException() {
        // Arrange & Act
        OrderNotFoundException orderException = new OrderNotFoundException("Order error");
        InvalidOrderParametersException paramException = new InvalidOrderParametersException("Param error");
        InvalidOrderStatusException statusException = new InvalidOrderStatusException("Status error");
        InsufficientFundsException fundsException = new InsufficientFundsException("Funds error");
        InvalidAccountException accountException = new InvalidAccountException("Account error");

        // Assert
        assertTrue(orderException instanceof RuntimeException);
        assertTrue(paramException instanceof RuntimeException);
        assertTrue(statusException instanceof RuntimeException);
        assertTrue(fundsException instanceof RuntimeException);
        assertTrue(accountException instanceof RuntimeException);
    }

    // ==================== Exception Chaining Tests ====================

    @Test
    @DisplayName("Should preserve exception cause chain")
    void testExceptionCauseChain() {
        // Arrange
        Throwable rootCause = new IllegalStateException("Root cause");
        Throwable middleCause = new RuntimeException("Middle cause", rootCause);
        String message = "Final error";

        // Act
        InvalidOrderParametersException exception = new InvalidOrderParametersException(message, middleCause);

        // Assert
        assertEquals(message, exception.getMessage());
        assertEquals(middleCause, exception.getCause());
        assertEquals(rootCause, exception.getCause().getCause());
    }

    // ==================== OrderId Field Tests ====================

    @Test
    @DisplayName("Should initialize orderId as null by default")
    void testOrderIdInitiallyNull() {
        // Act
        InvalidOrderParametersException exception1 = new InvalidOrderParametersException("Error");
        InsufficientFundsException exception2 = new InsufficientFundsException("Error");

        // Assert
        assertNull(exception1.getOrderId());
        assertNull(exception2.getOrderId());
    }

    @Test
    @DisplayName("Should support multiple orderId updates")
    void testMultipleOrderIdUpdates() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID orderId3 = UUID.randomUUID();

        // Act
        exception.setOrderId(orderId1);
        assertEquals(orderId1, exception.getOrderId());

        exception.setOrderId(orderId2);
        assertEquals(orderId2, exception.getOrderId());

        exception.setOrderId(orderId3);
        assertEquals(orderId3, exception.getOrderId());

        // Assert
        assertNotEquals(orderId1, exception.getOrderId());
        assertNotEquals(orderId2, exception.getOrderId());
        assertEquals(orderId3, exception.getOrderId());
    }

    // ==================== Exception Throwing Tests ====================

    @Test
    @DisplayName("Should be catchable as RuntimeException")
    void testCatchAsRuntimeException() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            throw exception;
        });
    }

    @Test
    @DisplayName("Should be catchable as its specific type")
    void testCatchAsSpecificType() {
        // Arrange
        InvalidOrderParametersException exception = new InvalidOrderParametersException("Error");

        // Act & Assert
        assertThrows(InvalidOrderParametersException.class, () -> {
            throw exception;
        });
    }

    @Test
    @DisplayName("Should preserve message through exception propagation")
    void testMessagePreservation() {
        // Arrange
        String originalMessage = "Original error message";
        InvalidOrderParametersException exception = new InvalidOrderParametersException(originalMessage);

        // Act & Assert
        try {
            throw exception;
        } catch (InvalidOrderParametersException e) {
            assertEquals(originalMessage, e.getMessage());
        }
    }

    // Helper class for testing
    private static class StateException extends Exception {
        StateException(String message) {
            super(message);
        }
    }
}
