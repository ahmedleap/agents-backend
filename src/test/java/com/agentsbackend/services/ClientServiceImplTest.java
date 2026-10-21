package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.DTO.response.ClientResponseDTO;
import com.agentsbackend.entities.Client;
import com.agentsbackend.enums.PortfolioSizeRange;
import com.agentsbackend.enums.RiskTolerance;
import com.agentsbackend.exceptions.ClientNotFoundException;
import com.agentsbackend.repos.transactional.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("Client Service Tests")
class ClientServiceImplTest {

    private ClientServiceImpl clientService;

    @Mock
    private ClientRepository clientRepository;

    private UUID clientId;
    private Client testClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientServiceImpl(clientRepository);

        clientId = UUID.randomUUID();

        // Create test client as stored after the update
        testClient = new Client();
        testClient.setClientId(clientId);
        testClient.setFirstName("Jane");
        testClient.setLastName("Doe");
        testClient.setEmail("jane@example.com");
        testClient.setPasswordHash("hash-not-returned");
        testClient.setRiskTolerance(RiskTolerance.MODERATE);
        testClient.setPortfolioSizeRange(PortfolioSizeRange.UNDER_50K);
    }

    // ==================== getClient Tests ====================

    @Test
    @DisplayName("Should return the client's profile fields")
    void testGetClient_Success() {
        // Setup
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

        // Execute
        ClientResponseDTO result = clientService.getClient(clientId);

        // Verify
        assertEquals(clientId.toString(), result.getClientId());
        assertEquals("Jane", result.getFirstName());
        assertNull(result.getMiddleName());
        assertEquals("Doe", result.getLastName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals(RiskTolerance.MODERATE, result.getRiskTolerance());
        assertEquals(PortfolioSizeRange.UNDER_50K, result.getPortfolioSizeRange());
        verify(clientRepository, times(1)).findById(clientId);
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException when the client doesn't exist")
    void testGetClient_ClientNotFound() {
        // Setup
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(ClientNotFoundException.class, () -> clientService.getClient(clientId));
    }

    // ==================== updateClient Tests ====================

    @Test
    @DisplayName("Should update the client and return the profile as stored after the update")
    void testUpdateClient_ReturnsStoredProfile() {
        // Setup
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setFirstName("Jane");

        when(clientRepository.updateClient(eq(clientId), any())).thenReturn(1);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

        // Execute
        ClientResponseDTO result = clientService.updateClient(clientId, request);

        // Verify
        assertEquals(clientId.toString(), result.getClientId());
        assertEquals("Jane", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals(RiskTolerance.MODERATE, result.getRiskTolerance());
        assertEquals(PortfolioSizeRange.UNDER_50K, result.getPortfolioSizeRange());
        verify(clientRepository, times(1)).updateClient(eq(clientId), any());
        verify(clientRepository, times(1)).findById(clientId);
    }

    @Test
    @DisplayName("Should trim name fields and pass fields that weren't sent through as null")
    void testUpdateClient_TrimsNames() {
        // Setup
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setFirstName("  Jane  ");
        request.setMiddleName(" Q ");

        when(clientRepository.updateClient(eq(clientId), any())).thenReturn(1);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

        // Execute
        clientService.updateClient(clientId, request);

        // Verify
        ArgumentCaptor<UpdateClientRequestDTO> sent = ArgumentCaptor.forClass(UpdateClientRequestDTO.class);
        verify(clientRepository).updateClient(eq(clientId), sent.capture());
        assertEquals("Jane", sent.getValue().getFirstName());
        assertEquals("Q", sent.getValue().getMiddleName());
        assertNull(sent.getValue().getLastName());
        assertNull(sent.getValue().getRiskTolerance());
    }

    @Test
    @DisplayName("Should send a blank middle name as \"\" so the repository clears it to NULL")
    void testUpdateClient_BlankMiddleNameClears() {
        // Setup
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setMiddleName("   ");

        when(clientRepository.updateClient(eq(clientId), any())).thenReturn(1);
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(testClient));

        // Execute
        ClientResponseDTO result = clientService.updateClient(clientId, request);

        // Verify
        ArgumentCaptor<UpdateClientRequestDTO> sent = ArgumentCaptor.forClass(UpdateClientRequestDTO.class);
        verify(clientRepository).updateClient(eq(clientId), sent.capture());
        assertEquals("", sent.getValue().getMiddleName());
        assertNull(result.getMiddleName());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when no fields are sent")
    void testUpdateClient_EmptyRequest() {
        assertThrows(IllegalArgumentException.class,
            () -> clientService.updateClient(clientId, new UpdateClientRequestDTO()));
        verifyNoInteractions(clientRepository);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when the request is null")
    void testUpdateClient_NullRequest() {
        assertThrows(IllegalArgumentException.class,
            () -> clientService.updateClient(clientId, null));
        verifyNoInteractions(clientRepository);
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException when no row was updated")
    void testUpdateClient_ClientNotFound() {
        // Setup
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setLastName("Doe");

        when(clientRepository.updateClient(eq(clientId), any())).thenReturn(0);

        // Execute & Verify
        assertThrows(ClientNotFoundException.class,
            () -> clientService.updateClient(clientId, request));
        verify(clientRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should throw ClientNotFoundException if the client is gone when it is re-read")
    void testUpdateClient_ClientMissingOnReRead() {
        // Setup
        UpdateClientRequestDTO request = new UpdateClientRequestDTO();
        request.setFirstName("Jane");

        when(clientRepository.updateClient(eq(clientId), any())).thenReturn(1);
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // Execute & Verify
        assertThrows(ClientNotFoundException.class,
            () -> clientService.updateClient(clientId, request));
    }
}
