package com.agentsbackend.controllers;

import com.agentsbackend.DTO.response.WatchlistResponse;
import com.agentsbackend.services.WatchlistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WatchlistControllerTest {

    private static final UUID CLIENT_ID = UUID.fromString("850e8400-e29b-41d4-a716-446655770001");
    private static final UUID INSTRUMENT_ID = UUID.fromString("650e8400-e29b-41d4-a716-446655550001");

    private MockMvc mockMvc;

        @Mock
    private WatchlistService watchlistService;

        @BeforeEach
        void setUp() {
                mockMvc = MockMvcBuilders.standaloneSetup(new WatchlistController(watchlistService)).build();
        }

    @Test
    void addReturnsCreatedWatchlistEntry() throws Exception {
        WatchlistResponse response = new WatchlistResponse(
                UUID.fromString("b50e8400-e29b-41d4-a716-446655aa0001"),
                CLIENT_ID,
                INSTRUMENT_ID,
                "AAPL",
                "Apple Inc.",
                LocalDateTime.parse("2026-09-21T12:00:00"));
        when(watchlistService.add(any())).thenReturn(response);

        mockMvc.perform(post("/api/watchlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientId": "850e8400-e29b-41d4-a716-446655770001",
                                  "instrumentId": "650e8400-e29b-41d4-a716-446655550001"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clientId").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$.instrumentId").value(INSTRUMENT_ID.toString()))
                .andExpect(jsonPath("$.ticker").value("AAPL"));
    }

            @Test
            void watchlistResponseExposesAllFieldsAndValueMethods() {
            UUID watchlistId = UUID.fromString("b50e8400-e29b-41d4-a716-446655aa0001");
            LocalDateTime addedAt = LocalDateTime.parse("2026-09-21T12:00:00");
            WatchlistResponse response = new WatchlistResponse(
                watchlistId, CLIENT_ID, INSTRUMENT_ID, "AAPL", "Apple Inc.", addedAt);
            WatchlistResponse equalResponse = new WatchlistResponse(
                watchlistId, CLIENT_ID, INSTRUMENT_ID, "AAPL", "Apple Inc.", addedAt);
            WatchlistResponse differentResponse = new WatchlistResponse(
                watchlistId, CLIENT_ID, INSTRUMENT_ID, "MSFT", "Microsoft", addedAt);

            assertEquals(watchlistId, response.watchlistId());
            assertEquals(CLIENT_ID, response.clientId());
            assertEquals(INSTRUMENT_ID, response.instrumentId());
            assertEquals("AAPL", response.ticker());
            assertEquals("Apple Inc.", response.instrumentName());
            assertEquals(addedAt, response.addedAt());
            assertEquals(response, equalResponse);
            assertEquals(response.hashCode(), equalResponse.hashCode());
            assertNotEquals(response, differentResponse);
            assertTrue(response.toString().contains("AAPL"));
            }

    @Test
    void findByClientIdReturnsWatchlistEntries() throws Exception {
        when(watchlistService.findByClientId(CLIENT_ID)).thenReturn(List.of(
                new WatchlistResponse(
                        UUID.fromString("b50e8400-e29b-41d4-a716-446655aa0001"),
                        CLIENT_ID,
                        INSTRUMENT_ID,
                        "AAPL",
                        "Apple Inc.",
                        LocalDateTime.parse("2026-09-21T12:00:00"))));

        mockMvc.perform(get("/api/watchlists/{clientId}", CLIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticker").value("AAPL"))
                .andExpect(jsonPath("$[0].instrumentName").value("Apple Inc."));
    }

    @Test
    void deleteReturnsNoContentWhenEntryExists() throws Exception {
        when(watchlistService.delete(eq(CLIENT_ID), eq(INSTRUMENT_ID))).thenReturn(true);

        mockMvc.perform(delete("/api/watchlists/{clientId}/{instrumentId}", CLIENT_ID, INSTRUMENT_ID))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturnsNotFoundWhenEntryDoesNotExist() throws Exception {
        when(watchlistService.delete(eq(CLIENT_ID), eq(INSTRUMENT_ID))).thenReturn(false);

        mockMvc.perform(delete("/api/watchlists/{clientId}/{instrumentId}", CLIENT_ID, INSTRUMENT_ID))
                .andExpect(status().isNotFound());
    }
}
