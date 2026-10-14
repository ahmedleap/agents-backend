package com.agentsbackend.repos;

import com.agentsbackend.entities.Client;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface ClientRepository {
    @Select("SELECT * FROM clients WHERE client_id = #{clientId}")
    Optional<Client> findById(UUID clientId);
}
