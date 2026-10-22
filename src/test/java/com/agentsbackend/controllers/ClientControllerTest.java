package com.agentsbackend.controllers;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.DTO.response.ClientResponseDTO;
import com.agentsbackend.enums.RiskTolerance;
import com.agentsbackend.services.ClientService;
import com.agentsbackend.services.MissionAuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.core.MethodParameter;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClientController Tests")
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private MissionAuthorizationService authorizationService;

    private MockMvc mockMvc;
    private UUID clientId;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        mockMvc = MockMvcBuilders.standaloneSetup(new ClientController(clientService, authorizationService))
                .setCustomArgumentResolvers(jwtResolver())
            .build();
    }

    private HandlerMethodArgumentResolver jwtResolver() {
        Jwt jwt = Jwt.withTokenValue("synthetic-token")
                .header("alg", "RS256")
                .subject(clientId.toString())
                .build();
        return new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.getParameterType().equals(Jwt.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          org.springframework.web.method.support.ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          org.springframework.web.bind.support.WebDataBinderFactory binderFactory)
                    throws Exception {
                return jwt;
            }
        };
    }

    private ClientResponseDTO response(String firstName, String lastName) {
        return new ClientResponseDTO(clientId.toString(), firstName, null, lastName,
                "jane@example.com", RiskTolerance.MODERATE, null);
    }

    @Test
    @DisplayName("Should get the client's profile and return 200 OK")
    void testGetClient_Success() throws Exception {
        when(authorizationService.clientId(any())).thenReturn(clientId);
        when(clientService.getClient(clientId)).thenReturn(response("Jane", "Doe"));

        mockMvc.perform(get("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("jane@example.com"))
                .andExpect(jsonPath("$.riskTolerance").value("MODERATE"));

        verify(authorizationService, times(1)).requireOwnClient(clientId, clientId);
        verify(clientService, times(1)).getClient(clientId);
    }

    @Test
    @DisplayName("Should return 403 when getting another client's profile")
    void testGetClient_OtherClientForbidden() throws Exception {
        UUID otherClientId = UUID.randomUUID();
        when(authorizationService.clientId(any())).thenReturn(clientId);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(authorizationService).requireOwnClient(clientId, otherClientId);

        mockMvc.perform(get("/api/client/v1/{clientId}", otherClientId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("Should update one field and return 200 OK")
    void testUpdateClient_OneField() throws Exception {
        when(clientService.updateClient(eq(clientId), any())).thenReturn(response("Janet", "Doe"));

        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Janet\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value(clientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Janet"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        ArgumentCaptor<UpdateClientRequestDTO> sent = ArgumentCaptor.forClass(UpdateClientRequestDTO.class);
        verify(clientService, times(1)).updateClient(eq(clientId), sent.capture());
        assertEquals("Janet", sent.getValue().getFirstName());
        assertNull(sent.getValue().getLastName());
    }

    @Test
    @DisplayName("Should update several fields and return 200 OK")
    void testUpdateClient_SeveralFields() throws Exception {
        when(clientService.updateClient(eq(clientId), any())).thenReturn(response("Jane", "Smith"));

        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastName\": \"Smith\", \"middleName\": \"Q\", \"riskTolerance\": \"AGGRESSIVE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Smith"));

        ArgumentCaptor<UpdateClientRequestDTO> sent = ArgumentCaptor.forClass(UpdateClientRequestDTO.class);
        verify(clientService, times(1)).updateClient(eq(clientId), sent.capture());
        assertEquals("Smith", sent.getValue().getLastName());
        assertEquals("Q", sent.getValue().getMiddleName());
        assertEquals(RiskTolerance.AGGRESSIVE, sent.getValue().getRiskTolerance());
    }

    @Test
    @DisplayName("Should check that the logged-in client is the client being updated")
    void testUpdateClient_ChecksOwnClient() throws Exception {
        when(authorizationService.clientId(any())).thenReturn(clientId);
        when(clientService.updateClient(eq(clientId), any())).thenReturn(response("Jane", "Doe"));

        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Jane\"}"))
                .andExpect(status().isOk());

        verify(authorizationService, times(1)).requireOwnClient(clientId, clientId);
    }

    @Test
    @DisplayName("Should return 403 when updating another client")
    void testUpdateClient_OtherClientForbidden() throws Exception {
        UUID otherClientId = UUID.randomUUID();
        when(authorizationService.clientId(any())).thenReturn(clientId);
        doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN))
                .when(authorizationService).requireOwnClient(clientId, otherClientId);

        mockMvc.perform(patch("/api/client/v1/{clientId}", otherClientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Jane\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("Should ignore phone and country in the request (not updatable)")
    void testUpdateClient_PhoneAndCountryIgnored() throws Exception {
        when(clientService.updateClient(eq(clientId), any())).thenReturn(response("Jane", "Doe"));

        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"Jane\", \"phone\": \"2025550123\", \"country\": \"US\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").doesNotExist())
                .andExpect(jsonPath("$.country").doesNotExist());

        verify(clientService, times(1)).updateClient(eq(clientId), any());
    }

    @Test
    @DisplayName("Should return 400 for a blank first name without calling the service")
    void testUpdateClient_BlankFirstName() throws Exception {
        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\": \"   \"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("Should return 400 for a name over 50 characters without calling the service")
    void testUpdateClient_NameTooLong() throws Exception {
        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lastName\": \"" + "x".repeat(51) + "\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientService);
    }

    @Test
    @DisplayName("Should return 400 for an unknown risk tolerance value")
    void testUpdateClient_UnknownRiskTolerance() throws Exception {
        mockMvc.perform(patch("/api/client/v1/{clientId}", clientId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"riskTolerance\": \"YOLO\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(clientService);
    }
}
