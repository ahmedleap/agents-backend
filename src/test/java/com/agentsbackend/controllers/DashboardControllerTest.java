package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.DashboardResponse;
import com.agentsbackend.services.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private MockMvc mockMvc;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();
        clientId = UUID.randomUUID();
    }

    @Test
    void returnsDashboardForClientAndPeriod() throws Exception {
        when(dashboardService.getDashboard(clientId, "90D"))
                .thenReturn(new DashboardResponse(List.of(), List.of(), List.of(), List.of(), List.of()));

        mockMvc.perform(get("/dashboard")
                        .param("clientId", clientId.toString())
                        .param("period", "90D"))
                .andExpect(status().isOk());

        verify(dashboardService).getDashboard(eq(clientId), eq("90D"));
    }
}