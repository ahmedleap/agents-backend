package com.agentsbackend.repos.transactional;

import com.agentsbackend.entities.Client;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    @Select("SELECT * FROM clients WHERE client_id = #{clientId}")
    Optional<Client> findById(UUID clientId);
}
