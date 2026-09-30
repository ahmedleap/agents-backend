package com.agentsbackend.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Should handle AccountNotFoundException with 404 status")
    void testHandleAccountNotFoundException() {
        UUID accountId = UUID.randomUUID();
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccountNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Account not found with ID: " + accountId, response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle ClientNotFoundException with 404 status")
    void testHandleClientNotFoundException() {
        UUID clientId = UUID.randomUUID();
        ClientNotFoundException exception = new ClientNotFoundException(clientId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleClientNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Client not found with ID: " + clientId, response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle InstrumentNotFoundException with 404 status")
    void testHandleInstrumentNotFoundException() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInstrumentNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Instrument was not found with Instrument Id: " + instrumentId, response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle HoldingNotFoundException with 404 status")
    void testHandleHoldingNotFoundException() {
        UUID holdingId = UUID.randomUUID();
        HoldingNotFoundException exception = new HoldingNotFoundException(holdingId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleHoldingNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Holding not found with ID: " + holdingId, response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle UnauthorizedAccountAccessException with 403 status")
    void testHandleUnauthorizedAccountAccessException() {
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UnauthorizedAccountAccessException exception = new UnauthorizedAccountAccessException(clientId, accountId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUnauthorizedAccountAccess(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("Account with ID: " + accountId + " does not belong to client with ID: " + clientId, response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with 400 status")
    void testHandleIllegalArgumentException() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid UUID format");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertTrue(response.getBody().get("error").contains("Invalid UUID format"));
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException with 409 status")
    void testHandleDuplicateKeyException() {
        DuplicateKeyException exception = new DuplicateKeyException("Duplicate key");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateKey(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("error"));
        assertEquals("This account already holds this instrument. Use PUT to update quantity instead.", response.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle AccountNotFoundException with proper error message format")
    void testHandleAccountNotFoundException_MessageFormat() {
        UUID accountId = UUID.randomUUID();
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccountNotFound(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertTrue(errorMessage.contains("Account not found"));
        assertTrue(errorMessage.contains(accountId.toString()));
    }

    @Test
    @DisplayName("Should handle ClientNotFoundException with proper error message format")
    void testHandleClientNotFoundException_MessageFormat() {
        UUID clientId = UUID.randomUUID();
        ClientNotFoundException exception = new ClientNotFoundException(clientId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleClientNotFound(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertTrue(errorMessage.contains("Client not found"));
        assertTrue(errorMessage.contains(clientId.toString()));
    }

    @Test
    @DisplayName("Should handle InstrumentNotFoundException with proper error message format")
    void testHandleInstrumentNotFoundException_MessageFormat() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleInstrumentNotFound(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertTrue(errorMessage.contains("Instrument"));
        assertTrue(errorMessage.contains(instrumentId.toString()));
    }

    @Test
    @DisplayName("Should return error map with single error key")
    void testExceptionResponse_ErrorMapStructure() {
        UUID accountId = UUID.randomUUID();
        AccountNotFoundException exception = new AccountNotFoundException(accountId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleAccountNotFound(exception);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().containsKey("error"));
    }

    @Test
    @DisplayName("Should handle AccountNotFoundException consistently across multiple calls")
    void testHandleAccountNotFoundException_Consistency() {
        UUID accountId = UUID.randomUUID();
        AccountNotFoundException exception1 = new AccountNotFoundException(accountId);
        AccountNotFoundException exception2 = new AccountNotFoundException(accountId);

        ResponseEntity<Map<String, String>> response1 = exceptionHandler.handleAccountNotFound(exception1);
        ResponseEntity<Map<String, String>> response2 = exceptionHandler.handleAccountNotFound(exception2);

        assertEquals(response1.getStatusCode(), response2.getStatusCode());
        assertEquals(response1.getBody().get("error"), response2.getBody().get("error"));
    }

    @Test
    @DisplayName("Should handle UnauthorizedAccountAccessException with correct status code")
    void testHandleUnauthorizedAccountAccessException_StatusCode() {
        UUID accountId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        UnauthorizedAccountAccessException exception = new UnauthorizedAccountAccessException(clientId, accountId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleUnauthorizedAccountAccess(exception);

        // 403 Forbidden is the correct status for unauthorized access
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle IllegalArgumentException with appropriate message")
    void testHandleIllegalArgumentException_Message() {
        String invalidMessage = "Invalid UUID";
        IllegalArgumentException exception = new IllegalArgumentException(invalidMessage);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgument(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertTrue(errorMessage.contains("Invalid UUID format"));
        assertTrue(errorMessage.contains(invalidMessage));
    }

    @Test
    @DisplayName("Should handle DuplicateKeyException with consistent message")
    void testHandleDuplicateKeyException_Message() {
        DuplicateKeyException exception = new DuplicateKeyException("Duplicate entry");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleDuplicateKey(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertEquals("This account already holds this instrument. Use PUT to update quantity instead.", errorMessage);
    }

    @Test
    @DisplayName("Should handle HoldingNotFoundException with proper error message format")
    void testHandleHoldingNotFoundException_MessageFormat() {
        UUID holdingId = UUID.randomUUID();
        HoldingNotFoundException exception = new HoldingNotFoundException(holdingId);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleHoldingNotFound(exception);

        assertNotNull(response.getBody());
        String errorMessage = response.getBody().get("error");
        assertTrue(errorMessage.contains("Holding not found"));
        assertTrue(errorMessage.contains(holdingId.toString()));
    }

    @Test
    @DisplayName("Should handle multiple exceptions with different status codes")
    void testHandleMultipleExceptions_DifferentStatusCodes() {
        UUID id = UUID.randomUUID();
        
        ResponseEntity<Map<String, String>> notFoundResponse = exceptionHandler.handleAccountNotFound(
            new AccountNotFoundException(id)
        );
        ResponseEntity<Map<String, String>> forbiddenResponse = exceptionHandler.handleUnauthorizedAccountAccess(
            new UnauthorizedAccountAccessException(id, id)
        );
        ResponseEntity<Map<String, String>> conflictResponse = exceptionHandler.handleDuplicateKey(
            new DuplicateKeyException("duplicate")
        );

        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, forbiddenResponse.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, conflictResponse.getStatusCode());
    }
}
