package com.agentsbackend.DTO.requests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GetPendingOrdersRequest DTO Tests")
class GetPendingOrdersRequestTest {

    @Test
    @DisplayName("Should create GetPendingOrdersRequest with default values")
    void testGetPendingOrdersRequestDefaults() {
        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Assert
        assertNull(request.getAccountId());
        assertEquals(50, request.getLimit());
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should create GetPendingOrdersRequest with all parameters")
    void testGetPendingOrdersRequestInitialization() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        Integer limit = 100;
        Integer offset = 10;

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, limit, offset);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(limit, request.getLimit());
        assertEquals(offset, request.getOffset());
    }

    @Test
    @DisplayName("Should set default limit when null provided")
    void testGetPendingOrdersRequestNullLimit() {
        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(UUID.randomUUID(), null, 0);

        // Assert
        assertEquals(50, request.getLimit());
    }

    @Test
    @DisplayName("Should set default offset when null provided")
    void testGetPendingOrdersRequestNullOffset() {
        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(UUID.randomUUID(), 50, null);

        // Assert
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should set default values when both limit and offset are null")
    void testGetPendingOrdersRequestBothNull() {
        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(UUID.randomUUID(), null, null);

        // Assert
        assertEquals(50, request.getLimit());
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should set and get account ID")
    void testGetPendingOrdersRequestAccountId() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
        UUID accountId = UUID.randomUUID();

        // Act
        request.setAccountId(accountId);

        // Assert
        assertEquals(accountId, request.getAccountId());
    }

    @Test
    @DisplayName("Should set and get limit")
    void testGetPendingOrdersRequestLimit() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setLimit(200);

        // Assert
        assertEquals(200, request.getLimit());
    }

    @Test
    @DisplayName("Should set and get offset")
    void testGetPendingOrdersRequestOffset() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setOffset(25);

        // Assert
        assertEquals(25, request.getOffset());
    }

    @Test
    @DisplayName("Should allow minimum limit of 0")
    void testGetPendingOrdersRequestMinimumLimit() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setLimit(0);

        // Assert
        assertEquals(0, request.getLimit());
    }

    @Test
    @DisplayName("Should allow maximum limit of 1000")
    void testGetPendingOrdersRequestMaximumLimit() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setLimit(1000);

        // Assert
        assertEquals(1000, request.getLimit());
    }

    @Test
    @DisplayName("Should allow minimum offset of 0")
    void testGetPendingOrdersRequestMinimumOffset() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setOffset(0);

        // Assert
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should allow large offset")
    void testGetPendingOrdersRequestLargeOffset() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setOffset(10000);

        // Assert
        assertEquals(10000, request.getOffset());
    }

    @Test
    @DisplayName("Should handle null limit in setter")
    void testGetPendingOrdersRequestSetNullLimit() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(UUID.randomUUID(), 100, 10);

        // Act
        request.setLimit(null);

        // Assert
        assertEquals(50, request.getLimit());
    }

    @Test
    @DisplayName("Should handle null offset in setter")
    void testGetPendingOrdersRequestSetNullOffset() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(UUID.randomUUID(), 100, 10);

        // Act
        request.setOffset(null);

        // Assert
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should filter by account ID only")
    void testGetPendingOrdersRequestFilterByAccountOnly() {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, null, null);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(50, request.getLimit());
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should retrieve all pending orders without account filter")
    void testGetPendingOrdersRequestNoFilter() {
        // Act
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(null, 50, 0);

        // Assert
        assertNull(request.getAccountId());
        assertEquals(50, request.getLimit());
        assertEquals(0, request.getOffset());
    }

    @Test
    @DisplayName("Should support pagination with limit and offset")
    void testGetPendingOrdersRequestPagination() {
        // Arrange
        UUID accountId = UUID.randomUUID();

        // Act
        GetPendingOrdersRequest page1 = new GetPendingOrdersRequest(accountId, 10, 0);
        GetPendingOrdersRequest page2 = new GetPendingOrdersRequest(accountId, 10, 10);
        GetPendingOrdersRequest page3 = new GetPendingOrdersRequest(accountId, 10, 20);

        // Assert
        assertEquals(10, page1.getLimit());
        assertEquals(0, page1.getOffset());
        assertEquals(10, page2.getLimit());
        assertEquals(10, page2.getOffset());
        assertEquals(10, page3.getLimit());
        assertEquals(20, page3.getOffset());
    }

    @Test
    @DisplayName("Should update all fields independently")
    void testGetPendingOrdersRequestUpdateFields() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();
        UUID accountId1 = UUID.randomUUID();
        UUID accountId2 = UUID.randomUUID();

        // Act
        request.setAccountId(accountId1);
        assertEquals(accountId1, request.getAccountId());
        
        request.setLimit(100);
        assertEquals(100, request.getLimit());
        
        request.setOffset(50);
        assertEquals(50, request.getOffset());
        
        request.setAccountId(accountId2);
        assertEquals(accountId2, request.getAccountId());

        // Assert
        assertEquals(accountId2, request.getAccountId());
        assertEquals(100, request.getLimit());
        assertEquals(50, request.getOffset());
    }

    @Test
    @DisplayName("Should handle typical pagination scenarios")
    void testGetPendingOrdersRequestTypicalScenarios() {
        // Scenario 1: Get first 50 results
        GetPendingOrdersRequest request1 = new GetPendingOrdersRequest();
        assertEquals(50, request1.getLimit());
        assertEquals(0, request1.getOffset());

        // Scenario 2: Get results for specific account with default pagination
        UUID accountId = UUID.randomUUID();
        GetPendingOrdersRequest request2 = new GetPendingOrdersRequest(accountId, null, null);
        assertEquals(accountId, request2.getAccountId());
        assertEquals(50, request2.getLimit());
        assertEquals(0, request2.getOffset());

        // Scenario 3: Get custom page size
        GetPendingOrdersRequest request3 = new GetPendingOrdersRequest(accountId, 25, 75);
        assertEquals(accountId, request3.getAccountId());
        assertEquals(25, request3.getLimit());
        assertEquals(75, request3.getOffset());
    }

    @Test
    @DisplayName("Should handle edge case: limit of 1")
    void testGetPendingOrdersRequestLimitOne() {
        // Arrange
        GetPendingOrdersRequest request = new GetPendingOrdersRequest();

        // Act
        request.setLimit(1);

        // Assert
        assertEquals(1, request.getLimit());
    }

    @Test
    @DisplayName("Should preserve account ID through updates")
    void testGetPendingOrdersRequestPreserveAccountId() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, 50, 0);

        // Act
        request.setLimit(100);
        request.setOffset(20);

        // Assert
        assertEquals(accountId, request.getAccountId());
        assertEquals(100, request.getLimit());
        assertEquals(20, request.getOffset());
    }

    @Test
    @DisplayName("Should handle resetting account ID to null")
    void testGetPendingOrdersRequestResetAccountId() {
        // Arrange
        UUID accountId = UUID.randomUUID();
        GetPendingOrdersRequest request = new GetPendingOrdersRequest(accountId, 50, 0);

        // Act
        request.setAccountId(null);

        // Assert
        assertNull(request.getAccountId());
    }
}
