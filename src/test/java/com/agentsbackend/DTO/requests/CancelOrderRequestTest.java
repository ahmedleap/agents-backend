package com.agentsbackend.DTO.requests;

import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CancelOrderRequest DTO Tests")
class CancelOrderRequestTest {

    @Test
    @DisplayName("Should create CancelOrderRequest with order ID")
    void testCancelOrderRequestInitialization() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should create CancelOrderRequest with order ID and cancellation reason")
    void testCancelOrderRequestWithReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String reason = "User requested cancellation";

        // Act
        CancelOrderRequest request = new CancelOrderRequest(orderId, reason);

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertEquals(reason, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should create CancelOrderRequest using constructor with null reason")
    void testCancelOrderRequestConstructorNullReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CancelOrderRequest request = new CancelOrderRequest(orderId, null);

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should create empty CancelOrderRequest")
    void testCancelOrderRequestEmpty() {
        // Act
        CancelOrderRequest request = new CancelOrderRequest();

        // Assert
        assertNull(request.getOrderId());
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should set and get order ID")
    void testCancelOrderRequestSetOrderId() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId = UUID.randomUUID();

        // Act
        request.setOrderId(orderId);

        // Assert
        assertEquals(orderId, request.getOrderId());
    }

    @Test
    @DisplayName("Should set and get cancellation reason")
    void testCancelOrderRequestSetReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        String reason = "Market conditions changed";

        // Act
        request.setCancellationReason(reason);

        // Assert
        assertEquals(reason, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should update order ID")
    void testCancelOrderRequestUpdateId() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        // Act
        request.setOrderId(orderId1);
        assertEquals(orderId1, request.getOrderId());
        
        request.setOrderId(orderId2);

        // Assert
        assertEquals(orderId2, request.getOrderId());
        assertNotEquals(orderId1, request.getOrderId());
    }

    @Test
    @DisplayName("Should handle null order ID")
    void testCancelOrderRequestNullId() {
        // Arrange & Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(null);

        // Assert
        assertNull(request.getOrderId());
    }

    @Test
    @DisplayName("Should reset order ID to null")
    void testCancelOrderRequestResetId() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId = UUID.randomUUID();

        // Act
        request.setOrderId(orderId);
        assertEquals(orderId, request.getOrderId());

        request.setOrderId(null);

        // Assert
        assertNull(request.getOrderId());
    }

    @Test
    @DisplayName("Should handle different order IDs")
    void testCancelOrderRequestDifferentIds() {
        // Arrange
        CancelOrderRequest request1 = new CancelOrderRequest();
        CancelOrderRequest request2 = new CancelOrderRequest();
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();

        // Act
        request1.setOrderId(orderId1);
        request2.setOrderId(orderId2);

        // Assert
        assertEquals(orderId1, request1.getOrderId());
        assertEquals(orderId2, request2.getOrderId());
        assertNotEquals(request1.getOrderId(), request2.getOrderId());
    }

    @Test
    @DisplayName("Should support cancelling pending orders")
    void testCancelOrderRequestPendingOrder() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        // Act
        CancelOrderRequest request = new CancelOrderRequest();
        request.setOrderId(orderId);

        // Assert
        assertNotNull(request.getOrderId());
        assertEquals(orderId, request.getOrderId());
    }

    @Test
    @DisplayName("Should support multiple cancel requests")
    void testCancelOrderRequestMultipleRequests() {
        // Arrange
        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID orderId3 = UUID.randomUUID();

        // Act
        CancelOrderRequest request1 = new CancelOrderRequest();
        request1.setOrderId(orderId1);

        CancelOrderRequest request2 = new CancelOrderRequest();
        request2.setOrderId(orderId2);

        CancelOrderRequest request3 = new CancelOrderRequest();
        request3.setOrderId(orderId3);

        // Assert
        assertEquals(orderId1, request1.getOrderId());
        assertEquals(orderId2, request2.getOrderId());
        assertEquals(orderId3, request3.getOrderId());
        assertNotEquals(request1.getOrderId(), request2.getOrderId());
        assertNotEquals(request2.getOrderId(), request3.getOrderId());
    }

    @Test
    @DisplayName("Should preserve order ID through multiple operations")
    void testCancelOrderRequestPreserveOrderId() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId = UUID.randomUUID();

        // Act
        request.setOrderId(orderId);
        UUID retrievedId = request.getOrderId();
        UUID retrievedId2 = request.getOrderId();

        // Assert
        assertEquals(orderId, retrievedId);
        assertEquals(retrievedId, retrievedId2);
        assertEquals(orderId, request.getOrderId());
    }

    @Test
    @DisplayName("Should handle cancellation reason with typical reasons")
    void testCancelOrderRequestTypicalReasons() {
        // Arrange
        CancelOrderRequest request1 = new CancelOrderRequest();
        CancelOrderRequest request2 = new CancelOrderRequest();
        CancelOrderRequest request3 = new CancelOrderRequest();

        // Act
        request1.setCancellationReason("Insufficient funds");
        request2.setCancellationReason("Market order limit exceeded");
        request3.setCancellationReason("User requested cancellation");

        // Assert
        assertEquals("Insufficient funds", request1.getCancellationReason());
        assertEquals("Market order limit exceeded", request2.getCancellationReason());
        assertEquals("User requested cancellation", request3.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle null cancellation reason")
    void testCancelOrderRequestNullReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();

        // Act
        request.setCancellationReason(null);

        // Assert
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should allow empty string as cancellation reason")
    void testCancelOrderRequestEmptyReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();

        // Act
        request.setCancellationReason("");

        // Assert
        assertEquals("", request.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle long cancellation reason")
    void testCancelOrderRequestLongReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        String longReason = "This is a very detailed cancellation reason that explains " +
                            "exactly why the order was cancelled and provides context about " +
                            "the situation that led to this cancellation decision";

        // Act
        request.setCancellationReason(longReason);

        // Assert
        assertEquals(longReason, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should update cancellation reason independently")
    void testCancelOrderRequestUpdateReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId = UUID.randomUUID();

        // Act
        request.setOrderId(orderId);
        request.setCancellationReason("Initial reason");
        assertEquals("Initial reason", request.getCancellationReason());

        request.setCancellationReason("Updated reason");
        assertEquals("Updated reason", request.getCancellationReason());

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertEquals("Updated reason", request.getCancellationReason());
    }

    @Test
    @DisplayName("Should create request from constructor with all fields")
    void testCancelOrderRequestConstructorAllFields() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        String reason = "Manual cancellation by user";

        // Act
        CancelOrderRequest request = new CancelOrderRequest(orderId, reason);

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertEquals(reason, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle reason with special characters")
    void testCancelOrderRequestReasonSpecialChars() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        String reasonWithSpecialChars = "Cancellation: Due to @#$% market changes & conditions";

        // Act
        request.setCancellationReason(reasonWithSpecialChars);

        // Assert
        assertEquals(reasonWithSpecialChars, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle multiple independent requests with different reasons")
    void testCancelOrderRequestMultipleWithReasons() {
        // Arrange
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID id3 = UUID.randomUUID();

        // Act
        CancelOrderRequest req1 = new CancelOrderRequest(id1, "Reason 1");
        CancelOrderRequest req2 = new CancelOrderRequest(id2, "Reason 2");
        CancelOrderRequest req3 = new CancelOrderRequest(id3, "Reason 3");

        // Assert
        assertEquals(id1, req1.getOrderId());
        assertEquals("Reason 1", req1.getCancellationReason());
        assertEquals(id2, req2.getOrderId());
        assertEquals("Reason 2", req2.getCancellationReason());
        assertEquals(id3, req3.getOrderId());
        assertEquals("Reason 3", req3.getCancellationReason());
    }

    @Test
    @DisplayName("Should support field independence")
    void testCancelOrderRequestFieldIndependence() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        UUID orderId = UUID.randomUUID();

        // Act
        request.setOrderId(orderId);
        assertEquals(orderId, request.getOrderId());
        assertNull(request.getCancellationReason());

        request.setCancellationReason("Test reason");
        assertEquals(orderId, request.getOrderId());
        assertEquals("Test reason", request.getCancellationReason());

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertEquals("Test reason", request.getCancellationReason());
    }

    @Test
    @DisplayName("Should clear reason independently of order ID")
    void testCancelOrderRequestClearReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        CancelOrderRequest request = new CancelOrderRequest(orderId, "Initial reason");

        // Act
        request.setCancellationReason(null);

        // Assert
        assertEquals(orderId, request.getOrderId());
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should preserve order ID when clearing reason")
    void testCancelOrderRequestPreserveIdWhenClearingReason() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        CancelOrderRequest request = new CancelOrderRequest(orderId, "Initial reason");

        // Act
        request.setCancellationReason(null);
        UUID retrievedId = request.getOrderId();

        // Assert
        assertEquals(orderId, retrievedId);
        assertNull(request.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle whitespace in cancellation reason")
    void testCancelOrderRequestReasonWithWhitespace() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        String reasonWithWhitespace = "  Reason with spaces  ";

        // Act
        request.setCancellationReason(reasonWithWhitespace);

        // Assert
        assertEquals(reasonWithWhitespace, request.getCancellationReason());
    }

    @Test
    @DisplayName("Should handle multiline cancellation reason")
    void testCancelOrderRequestMultilineReason() {
        // Arrange
        CancelOrderRequest request = new CancelOrderRequest();
        String multilineReason = "Line 1\nLine 2\nLine 3";

        // Act
        request.setCancellationReason(multilineReason);

        // Assert
        assertEquals(multilineReason, request.getCancellationReason());
    }
}
