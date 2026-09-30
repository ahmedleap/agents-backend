package com.agentsbackend.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("InstrumentNotFoundException Tests")
class InstrumentNotFoundExceptionTest {

    @Test
    @DisplayName("Should throw InstrumentNotFoundException with instrument ID")
    void testInstrumentNotFoundException_WithId() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        assertNotNull(exception);
        assertTrue(exception.getMessage().contains("Instrument was not found"));
        assertTrue(exception.getMessage().contains(instrumentId.toString()));
    }

    @Test
    @DisplayName("Should contain proper message format")
    void testInstrumentNotFoundException_MessageFormat() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        String expectedMessage = "Instrument was not found with Instrument Id: " + instrumentId;
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should be a RuntimeException subclass")
    void testInstrumentNotFoundException_IsRuntimeException() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("Should throw exception when triggered")
    void testInstrumentNotFoundException_Throwable() {
        UUID instrumentId = UUID.randomUUID();

        assertThrows(InstrumentNotFoundException.class, () -> {
            throw new InstrumentNotFoundException(instrumentId);
        });
    }

    @Test
    @DisplayName("Should throw exception with message when triggered")
    void testInstrumentNotFoundException_ThrowWithMessage() {
        UUID instrumentId = UUID.randomUUID();
        String expectedMessage = "Instrument was not found with Instrument Id: " + instrumentId;

        InstrumentNotFoundException exception = assertThrows(InstrumentNotFoundException.class, () -> {
            throw new InstrumentNotFoundException(instrumentId);
        });

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Should maintain consistent message across multiple instances")
    void testInstrumentNotFoundException_ConsistentMessage() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception1 = new InstrumentNotFoundException(instrumentId);
        InstrumentNotFoundException exception2 = new InstrumentNotFoundException(instrumentId);

        assertEquals(exception1.getMessage(), exception2.getMessage());
    }

    @Test
    @DisplayName("Should have different messages for different instrument IDs")
    void testInstrumentNotFoundException_DifferentIds() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();

        InstrumentNotFoundException exception1 = new InstrumentNotFoundException(id1);
        InstrumentNotFoundException exception2 = new InstrumentNotFoundException(id2);

        assertNotEquals(exception1.getMessage(), exception2.getMessage());
        assertTrue(exception1.getMessage().contains(id1.toString()));
        assertTrue(exception2.getMessage().contains(id2.toString()));
    }

    @Test
    @DisplayName("Should be catchable as RuntimeException")
    void testInstrumentNotFoundException_CatchAsRuntime() {
        UUID instrumentId = UUID.randomUUID();

        try {
            throw new InstrumentNotFoundException(instrumentId);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Instrument was not found"));
        }
    }

    @Test
    @DisplayName("Should be catchable as Exception")
    void testInstrumentNotFoundException_CatchAsException() {
        UUID instrumentId = UUID.randomUUID();

        try {
            throw new InstrumentNotFoundException(instrumentId);
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("Instrument was not found"));
        }
    }

    @Test
    @DisplayName("Should not be null when instantiated")
    void testInstrumentNotFoundException_NotNull() {
        UUID instrumentId = UUID.randomUUID();
        InstrumentNotFoundException exception = new InstrumentNotFoundException(instrumentId);

        assertNotNull(exception);
        assertNotNull(exception.getMessage());
    }
}
