package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.DTO.response.ClientResponseDTO;
import java.util.UUID;

public interface ClientService {
    ClientResponseDTO getClient(UUID clientId);
    ClientResponseDTO updateClient(UUID clientId, UpdateClientRequestDTO request);
}
