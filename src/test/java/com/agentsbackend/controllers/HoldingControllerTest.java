package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.GetHoldingResponseDTO;
import com.agentsbackend.services.HoldingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("HoldingController Tests")
class HoldingControllerTest {

    @Mock
    private HoldingService holdingService;

    private MockMvc mockMvc;

    private UUID accountId;
    private UUID holdingId;
    private UUID instrumentId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new HoldingController(holdingService)).build();
        accountId = UUID.randomUUID();
        holdingId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get all holdings by account ID and return 200 OK")
    void testGetHoldings_Success() throws Exception {
        GetHoldingResponseDTO holding1 = createHoldingDTO("AAPL", "Apple Inc", new BigDecimal("100"));
        GetHoldingResponseDTO holding2 = createHoldingDTO("MSFT", "Microsoft Inc", new BigDecimal("50"));

        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding1, holding2));

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[1].ticker").value("MSFT"));

        verify(holdingService, times(1)).getHoldingsDTOByAccountId(accountId);
    }

    @Test
    @DisplayName("Should get holdings with empty list and return 200 OK")
    void testGetHoldings_EmptyList() throws Exception {
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(holdingService, times(1)).getHoldingsDTOByAccountId(accountId);
    }

    @Test
    @DisplayName("Should return correct HTTP status 200 for get holdings")
    void testGetHoldings_Status200() throws Exception {
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Should return correct content type for get holdings")
    void testGetHoldings_ContentType() throws Exception {
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should get single holding by account and holding ID and return 200 OK")
    void testGetOneHolding_Success() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("GOOGL", "Google Inc", new BigDecimal("25"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("GOOGL"))
                .andExpect(jsonPath("$.name").value("Google Inc"));

        verify(holdingService, times(1)).getOneHoldingDTO(accountId, holdingId);
    }

    @Test
    @DisplayName("Should return correct HTTP status 200 for get one holding")
    void testGetOneHolding_Status200() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("TSLA", "Tesla Inc", new BigDecimal("10"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId))
                .andExpect(status().is(200));
    }

    @Test
    @DisplayName("Should return correct content type for get one holding")
    void testGetOneHolding_ContentType() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("AMZN", "Amazon Inc", new BigDecimal("5"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should call service method once for get holdings")
    void testGetHoldings_ServiceMethodCalledOnce() throws Exception {
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId));

        verify(holdingService, times(1)).getHoldingsDTOByAccountId(accountId);
        verifyNoMoreInteractions(holdingService);
    }

    @Test
    @DisplayName("Should call service method once for get one holding")
    void testGetOneHolding_ServiceMethodCalledOnce() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("META", "Meta Inc", new BigDecimal("15"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId));

        verify(holdingService, times(1)).getOneHoldingDTO(accountId, holdingId);
        verifyNoMoreInteractions(holdingService);
    }

    @Test
    @DisplayName("Should pass correct UUID path variables to service for get holdings")
    void testGetHoldings_CorrectPathVariables() throws Exception {
        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId));

        verify(holdingService).getHoldingsDTOByAccountId(accountId);
    }

    @Test
    @DisplayName("Should pass correct UUID path variables to service for get one holding")
    void testGetOneHolding_CorrectPathVariables() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("NFLX", "Netflix Inc", new BigDecimal("2"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId));

        verify(holdingService).getOneHoldingDTO(accountId, holdingId);
    }

    @Test
    @DisplayName("Should get multiple holdings with correct field mapping")
    void testGetHoldings_FieldMapping() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("NVDA", "NVIDIA Inc", new BigDecimal("30"));

        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("NVDA"))
                .andExpect(jsonPath("$[0].name").value("NVIDIA Inc"))
                .andExpect(jsonPath("$[0].quantity").value(30));
    }

    @Test
    @DisplayName("Should get single holding with all response fields")
    void testGetOneHolding_AllFields() throws Exception {
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setHoldingId(holdingId.toString());
        holding.setAccountId(accountId.toString());
        holding.setInstrumentId(instrumentId.toString());
        holding.setTicker("ORCL");
        holding.setName("Oracle Corp");
        holding.setQuantity(new BigDecimal("40"));
        holding.setAverageCostBasis(new BigDecimal("100.00"));
        holding.setCurrentPrice(new BigDecimal("105.00"));
        holding.setCurrentValue(new BigDecimal("4200.00"));
        holding.setGainLossDollars(new BigDecimal("200.00"));
        holding.setGainLossPercent(new BigDecimal("5.00"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.holdingId").value(holdingId.toString()))
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.instrumentId").value(instrumentId.toString()))
                .andExpect(jsonPath("$.ticker").value("ORCL"))
                .andExpect(jsonPath("$.name").value("Oracle Corp"))
                .andExpect(jsonPath("$.quantity").value(40))
                .andExpect(jsonPath("$.averageCostBasis").value(100))
                .andExpect(jsonPath("$.currentPrice").value(105))
                .andExpect(jsonPath("$.currentValue").value(4200))
                .andExpect(jsonPath("$.gainLossDollars").value(200))
                .andExpect(jsonPath("$.gainLossPercent").value(5));
    }

    @Test
    @DisplayName("Should handle holdings with fractional quantities")
    void testGetHoldings_FractionalQuantities() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("BRK.B", "Berkshire Hathaway", new BigDecimal("0.5"));

        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(0.5));
    }

    @Test
    @DisplayName("Should handle holdings with large quantities")
    void testGetHoldings_LargeQuantities() throws Exception {
        GetHoldingResponseDTO holding = createHoldingDTO("SPY", "S&P 500 ETF", new BigDecimal("1000"));

        when(holdingService.getHoldingsDTOByAccountId(accountId)).thenReturn(List.of(holding));

        mockMvc.perform(get("/api/holding/v1/{accountId}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantity").value(1000));
    }

    @Test
    @DisplayName("Should handle single holding with large portfolio value")
    void testGetOneHolding_LargeValue() throws Exception {
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setTicker("BRK.A");
        holding.setName("Berkshire Hathaway A");
        holding.setQuantity(new BigDecimal("1"));
        holding.setCurrentValue(new BigDecimal("500000.00"));

        when(holdingService.getOneHoldingDTO(accountId, holdingId)).thenReturn(holding);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holdingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentValue").value(500000));
    }

    @Test
    @DisplayName("Should handle holdings endpoint with different account IDs")
    void testGetHoldings_DifferentAccountIds() throws Exception {
        UUID account1 = UUID.randomUUID();
        UUID account2 = UUID.randomUUID();
        GetHoldingResponseDTO holding1 = createHoldingDTO("AAPL", "Apple Inc", new BigDecimal("100"));
        GetHoldingResponseDTO holding2 = createHoldingDTO("GOOGL", "Google Inc", new BigDecimal("50"));

        when(holdingService.getHoldingsDTOByAccountId(account1)).thenReturn(List.of(holding1));
        when(holdingService.getHoldingsDTOByAccountId(account2)).thenReturn(List.of(holding2));

        mockMvc.perform(get("/api/holding/v1/{accountId}", account1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"));

        mockMvc.perform(get("/api/holding/v1/{accountId}", account2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("GOOGL"));
    }

    @Test
    @DisplayName("Should handle one holding endpoint with different holding IDs")
    void testGetOneHolding_DifferentHoldingIds() throws Exception {
        UUID holding1Id = UUID.randomUUID();
        UUID holding2Id = UUID.randomUUID();
        GetHoldingResponseDTO holding1 = createHoldingDTO("MSFT", "Microsoft", new BigDecimal("75"));
        GetHoldingResponseDTO holding2 = createHoldingDTO("TSLA", "Tesla", new BigDecimal("20"));

        when(holdingService.getOneHoldingDTO(accountId, holding1Id)).thenReturn(holding1);
        when(holdingService.getOneHoldingDTO(accountId, holding2Id)).thenReturn(holding2);

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holding1Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("MSFT"));

        mockMvc.perform(get("/api/holding/v1/{accountId}/{holdingId}", accountId, holding2Id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticker").value("TSLA"));
    }

    // Helper method to create test holding DTOs
    private GetHoldingResponseDTO createHoldingDTO(String ticker, String name, BigDecimal quantity) {
        GetHoldingResponseDTO holding = new GetHoldingResponseDTO();
        holding.setHoldingId(UUID.randomUUID().toString());
        holding.setAccountId(accountId.toString());
        holding.setInstrumentId(UUID.randomUUID().toString());
        holding.setTicker(ticker);
        holding.setName(name);
        holding.setQuantity(quantity);
        holding.setAverageCostBasis(new BigDecimal("100.00"));
        holding.setCurrentPrice(new BigDecimal("105.00"));
        holding.setCurrentValue(quantity.multiply(new BigDecimal("105.00")));
        holding.setGainLossDollars(quantity.multiply(new BigDecimal("5.00")));
        holding.setGainLossPercent(new BigDecimal("5.00"));
        return holding;
    }
}
