package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.GetAccountPortfolioResponseDTO;
import com.agentsbackend.DTO.response.EntirePortfolioResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO.IndustryAllocationDTO;
import com.agentsbackend.DTO.response.GetAllocationResponseDTO.AssetClassAllocationDTO;
import com.agentsbackend.services.PortfolioService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@DisplayName("PortfolioController Tests")
class PortfolioControllerTest {

    @Mock
    private PortfolioService portfolioService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    private UUID clientId;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new PortfolioController(portfolioService)).build();
        objectMapper = new ObjectMapper();
        clientId = UUID.randomUUID();
        accountId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should get portfolio by account ID and return 200 OK")
    void testGetPortfolioByAccountId_Success() throws Exception {
        GetAccountPortfolioResponseDTO response = new GetAccountPortfolioResponseDTO();
        response.setAccountId(accountId.toString());
        response.setTotalPortfolioValue(new BigDecimal("10000.00"));
        response.setHoldings(new ArrayList<>());

        when(portfolioService.getPortfolioByAccountId(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}/{accountId}", clientId, accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountId").value(accountId.toString()))
                .andExpect(jsonPath("$.totalPortfolioValue").value(10000));

        verify(portfolioService, times(1)).getPortfolioByAccountId(clientId, accountId);
    }

    @Test
    @DisplayName("Should get entire portfolio by client ID and return 200 OK")
    void testGetEntirePortfolio_Success() throws Exception {
        EntirePortfolioResponseDTO response = new EntirePortfolioResponseDTO();
        response.setClientId(clientId.toString());
        response.setTotalPortfolioValue(new BigDecimal("50000.00"));
        response.setAccounts(new ArrayList<>());

        when(portfolioService.getEntirePortfolioByClientId(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.totalPortfolioValue").value(50000));

        verify(portfolioService, times(1)).getEntirePortfolioByClientId(clientId);
    }

    @Test
    @DisplayName("Should get portfolio allocation by client ID and return 200 OK")
    void testGetPortfolioAllocation_Success() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        List<IndustryAllocationDTO> industryBreakdown = new ArrayList<>();
        IndustryAllocationDTO industryAlloc = new IndustryAllocationDTO();
        industryAlloc.setIndustry("Technology");
        industryAlloc.setPercentage(new BigDecimal("50.00"));
        industryAlloc.setValue(new BigDecimal("5000.00"));
        industryBreakdown.add(industryAlloc);
        response.setIndustryBreakdown(industryBreakdown);

        List<AssetClassAllocationDTO> assetClassBreakdown = new ArrayList<>();
        AssetClassAllocationDTO assetClassAlloc = new AssetClassAllocationDTO();
        assetClassAlloc.setAssetClass("STOCK");
        assetClassAlloc.setPercentage(new BigDecimal("100.00"));
        assetClassAlloc.setValue(new BigDecimal("10000.00"));
        assetClassBreakdown.add(assetClassAlloc);
        response.setAssetClassBreakdown(assetClassBreakdown);

        when(portfolioService.getPortfolioAllocation(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.industryBreakdown").isArray())
                .andExpect(jsonPath("$.industryBreakdown[0].industry").value("Technology"))
                .andExpect(jsonPath("$.industryBreakdown[0].percentage").value(50))
                .andExpect(jsonPath("$.assetClassBreakdown").isArray())
                .andExpect(jsonPath("$.assetClassBreakdown[0].assetClass").value("STOCK"));

        verify(portfolioService, times(1)).getPortfolioAllocation(clientId);
    }

    @Test
    @DisplayName("Should get account allocation by client and account ID and return 200 OK")
    void testGetAccountAllocation_Success() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        List<IndustryAllocationDTO> industryBreakdown = new ArrayList<>();
        IndustryAllocationDTO industryAlloc = new IndustryAllocationDTO();
        industryAlloc.setIndustry("Finance");
        industryAlloc.setPercentage(new BigDecimal("100.00"));
        industryAlloc.setValue(new BigDecimal("5000.00"));
        industryBreakdown.add(industryAlloc);
        response.setIndustryBreakdown(industryBreakdown);

        List<AssetClassAllocationDTO> assetClassBreakdown = new ArrayList<>();
        AssetClassAllocationDTO assetClassAlloc = new AssetClassAllocationDTO();
        assetClassAlloc.setAssetClass("BOND");
        assetClassAlloc.setPercentage(new BigDecimal("100.00"));
        assetClassAlloc.setValue(new BigDecimal("5000.00"));
        assetClassBreakdown.add(assetClassAlloc);
        response.setAssetClassBreakdown(assetClassBreakdown);

        when(portfolioService.getAccountAllocation(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}/{accountId}", clientId, accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.industryBreakdown").isArray())
                .andExpect(jsonPath("$.industryBreakdown[0].industry").value("Finance"))
                .andExpect(jsonPath("$.assetClassBreakdown").isArray())
                .andExpect(jsonPath("$.assetClassBreakdown[0].assetClass").value("BOND"));

        verify(portfolioService, times(1)).getAccountAllocation(clientId, accountId);
    }

    @Test
    @DisplayName("Should return 200 OK for account portfolio with correct response headers")
    void testGetPortfolioByAccountId_ResponseHeaders() throws Exception {
        GetAccountPortfolioResponseDTO response = new GetAccountPortfolioResponseDTO();
        response.setAccountId(accountId.toString());
        response.setTotalPortfolioValue(new BigDecimal("10000.00"));

        when(portfolioService.getPortfolioByAccountId(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}/{accountId}", clientId, accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return 200 OK for entire portfolio with correct response headers")
    void testGetEntirePortfolio_ResponseHeaders() throws Exception {
        EntirePortfolioResponseDTO response = new EntirePortfolioResponseDTO();
        response.setClientId(clientId.toString());
        response.setTotalPortfolioValue(new BigDecimal("50000.00"));

        when(portfolioService.getEntirePortfolioByClientId(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return 200 OK for portfolio allocation with correct response headers")
    void testGetPortfolioAllocation_ResponseHeaders() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        response.setIndustryBreakdown(new ArrayList<>());
        response.setAssetClassBreakdown(new ArrayList<>());

        when(portfolioService.getPortfolioAllocation(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should return 200 OK for account allocation with correct response headers")
    void testGetAccountAllocation_ResponseHeaders() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        response.setIndustryBreakdown(new ArrayList<>());
        response.setAssetClassBreakdown(new ArrayList<>());

        when(portfolioService.getAccountAllocation(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}/{accountId}", clientId, accountId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should call service method once for get portfolio by account ID")
    void testGetPortfolioByAccountId_ServiceMethodCalledOnce() throws Exception {
        GetAccountPortfolioResponseDTO response = new GetAccountPortfolioResponseDTO();
        response.setAccountId(accountId.toString());

        when(portfolioService.getPortfolioByAccountId(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}/{accountId}", clientId, accountId));

        verify(portfolioService, times(1)).getPortfolioByAccountId(clientId, accountId);
        verifyNoMoreInteractions(portfolioService);
    }

    @Test
    @DisplayName("Should call service method once for get entire portfolio")
    void testGetEntirePortfolio_ServiceMethodCalledOnce() throws Exception {
        EntirePortfolioResponseDTO response = new EntirePortfolioResponseDTO();
        response.setClientId(clientId.toString());

        when(portfolioService.getEntirePortfolioByClientId(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}", clientId));

        verify(portfolioService, times(1)).getEntirePortfolioByClientId(clientId);
        verifyNoMoreInteractions(portfolioService);
    }

    @Test
    @DisplayName("Should call service method once for get portfolio allocation")
    void testGetPortfolioAllocation_ServiceMethodCalledOnce() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        response.setIndustryBreakdown(new ArrayList<>());
        response.setAssetClassBreakdown(new ArrayList<>());

        when(portfolioService.getPortfolioAllocation(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}", clientId));

        verify(portfolioService, times(1)).getPortfolioAllocation(clientId);
        verifyNoMoreInteractions(portfolioService);
    }

    @Test
    @DisplayName("Should call service method once for get account allocation")
    void testGetAccountAllocation_ServiceMethodCalledOnce() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        response.setIndustryBreakdown(new ArrayList<>());
        response.setAssetClassBreakdown(new ArrayList<>());

        when(portfolioService.getAccountAllocation(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}/{accountId}", clientId, accountId));

        verify(portfolioService, times(1)).getAccountAllocation(clientId, accountId);
        verifyNoMoreInteractions(portfolioService);
    }

    @Test
    @DisplayName("Should pass correct UUID path variables to service method")
    void testGetPortfolioByAccountId_CorrectPathVariables() throws Exception {
        GetAccountPortfolioResponseDTO response = new GetAccountPortfolioResponseDTO();

        when(portfolioService.getPortfolioByAccountId(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/{clientId}/{accountId}", clientId, accountId));

        verify(portfolioService).getPortfolioByAccountId(clientId, accountId);
    }

    @Test
    @DisplayName("Should handle multiple industry breakdowns in allocation response")
    void testGetPortfolioAllocation_MultipleIndustries() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();
        
        List<IndustryAllocationDTO> industryBreakdown = new ArrayList<>();
        IndustryAllocationDTO tech = new IndustryAllocationDTO();
        tech.setIndustry("Technology");
        tech.setPercentage(new BigDecimal("40.00"));
        tech.setValue(new BigDecimal("4000.00"));
        industryBreakdown.add(tech);

        IndustryAllocationDTO finance = new IndustryAllocationDTO();
        finance.setIndustry("Finance");
        finance.setPercentage(new BigDecimal("60.00"));
        finance.setValue(new BigDecimal("6000.00"));
        industryBreakdown.add(finance);

        response.setIndustryBreakdown(industryBreakdown);
        response.setAssetClassBreakdown(new ArrayList<>());

        when(portfolioService.getPortfolioAllocation(clientId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}", clientId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.industryBreakdown").isArray())
                .andExpect(jsonPath("$.industryBreakdown", hasSize(2)))
                .andExpect(jsonPath("$.industryBreakdown[0].industry").value("Technology"))
                .andExpect(jsonPath("$.industryBreakdown[1].industry").value("Finance"));
    }

    @Test
    @DisplayName("Should handle multiple asset class breakdowns in allocation response")
    void testGetAccountAllocation_MultipleAssetClasses() throws Exception {
        GetAllocationResponseDTO response = new GetAllocationResponseDTO();

        List<AssetClassAllocationDTO> assetClassBreakdown = new ArrayList<>();
        AssetClassAllocationDTO stock = new AssetClassAllocationDTO();
        stock.setAssetClass("STOCK");
        stock.setPercentage(new BigDecimal("60.00"));
        stock.setValue(new BigDecimal("3000.00"));
        assetClassBreakdown.add(stock);

        AssetClassAllocationDTO bond = new AssetClassAllocationDTO();
        bond.setAssetClass("BOND");
        bond.setPercentage(new BigDecimal("40.00"));
        bond.setValue(new BigDecimal("2000.00"));
        assetClassBreakdown.add(bond);

        response.setIndustryBreakdown(new ArrayList<>());
        response.setAssetClassBreakdown(assetClassBreakdown);

        when(portfolioService.getAccountAllocation(clientId, accountId)).thenReturn(response);

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}/{accountId}", clientId, accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assetClassBreakdown", hasSize(2)))
                .andExpect(jsonPath("$.assetClassBreakdown[0].assetClass").value("STOCK"))
                .andExpect(jsonPath("$.assetClassBreakdown[1].assetClass").value("BOND"));
    }

    @Test
    @DisplayName("Should return status OK with correct HTTP status code 200")
    void testPortfolioEndpoints_ReturnHttpStatus200() throws Exception {
        when(portfolioService.getPortfolioByAccountId(any(), any())).thenReturn(new GetAccountPortfolioResponseDTO());
        when(portfolioService.getEntirePortfolioByClientId(any())).thenReturn(new EntirePortfolioResponseDTO());
        when(portfolioService.getPortfolioAllocation(any())).thenReturn(new GetAllocationResponseDTO());
        when(portfolioService.getAccountAllocation(any(), any())).thenReturn(new GetAllocationResponseDTO());

        mockMvc.perform(get("/api/portfolio/v1/{clientId}/{accountId}", clientId, accountId))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/portfolio/v1/{clientId}", clientId))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}", clientId))
                .andExpect(status().is(200));

        mockMvc.perform(get("/api/portfolio/v1/allocation/{clientId}/{accountId}", clientId, accountId))
                .andExpect(status().is(200));
    }
}
