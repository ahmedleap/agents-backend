package com.agentsbackend.services;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.DTO.response.ClientResponseDTO;
import com.agentsbackend.entities.Client;
import com.agentsbackend.exceptions.ClientNotFoundException;
import com.agentsbackend.repos.transactional.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@Transactional
public class ClientServiceImpl implements ClientService{
    private final ClientRepository clientRepository;

    public ClientServiceImpl(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public ClientResponseDTO getClient(UUID clientId) {
        // Retrieve client from database, throw exception if not found
        Client client = clientRepository.findById(clientId)
            .orElseThrow(() -> new ClientNotFoundException(clientId));

        return clientToDTO(client);
    }

    @Override
    public ClientResponseDTO updateClient(UUID clientId, UpdateClientRequestDTO request) {
        // Reject a request with no fields instead of running an UPDATE that changes nothing
        if (request == null || !request.hasAnyField()) {
            throw new IllegalArgumentException("Nothing to update: send at least one field");
        }

        // Update only the fields that were sent; 0 rows updated means the client doesn't exist
        if (clientRepository.updateClient(clientId, trimmed(request)) == 0) {
            throw new ClientNotFoundException(clientId);
        }

        // Re-read the client so the response shows exactly what is now stored
        return getClient(clientId);
    }

    /**
     * Converts a Client entity to a response DTO.
     * Only copies the profile fields shown on the settings page, never the password hash, SSN or tokens.
     *
     * @param client the client entity to convert
     * @return DTO containing the client's profile details
     */
    private ClientResponseDTO clientToDTO(Client client){
        return new ClientResponseDTO(
            client.getClientId().toString(),
            client.getFirstName(),
            client.getMiddleName(),
            client.getLastName(),
            client.getEmail(),
            client.getRiskTolerance(),
            client.getPortfolioSizeRange()
        );
    }

    /**
     * Returns a copy of the request with surrounding spaces removed from the name fields.
     * A middle name of only spaces becomes "" so the repository clears it to NULL.
     *
     * @param request the update request from the client
     * @return copy of the request with trimmed names (null fields stay null)
     */
    private UpdateClientRequestDTO trimmed(UpdateClientRequestDTO request){
        return new UpdateClientRequestDTO(
            trim(request.getFirstName()),
            trim(request.getMiddleName()),
            trim(request.getLastName()),
            request.getRiskTolerance(),
            request.getPortfolioSizeRange()
        );
    }

    private String trim(String value){
        return value == null ? null : value.trim();
    }
}
