package com.agentsbackend.repos.transactional;

import com.agentsbackend.DTO.requests.UpdateClientRequestDTO;
import com.agentsbackend.entities.Client;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {
    @Select("SELECT * FROM clients WHERE client_id = #{clientId}")
    Optional<Client> findById(UUID clientId);

    /**
     * Updates only the profile fields that were sent (non-null); everything else is left as it is.
     * An empty string clears the optional middle_name column via NULLIF.
     * Enum columns need CAST because the type handlers send enum values as VARCHAR.
     *
     * @return number of rows updated: 1 if the client exists, 0 if not
     */
    @Update({"<script>",
            "UPDATE clients",
            "<set>",
            "  <if test='req.firstName != null'>first_name = #{req.firstName},</if>",
            "  <if test='req.middleName != null'>middle_name = NULLIF(#{req.middleName}, ''),</if>",
            "  <if test='req.lastName != null'>last_name = #{req.lastName},</if>",
            "  <if test='req.riskTolerance != null'>risk_tolerance = CAST(#{req.riskTolerance} AS risk_tolerance),</if>",
            "  <if test='req.portfolioSizeRange != null'>portfolio_size_range = CAST(#{req.portfolioSizeRange} AS portfolio_size_range),</if>",
            "</set>",
            "WHERE client_id = #{clientId,jdbcType=OTHER}",
            "</script>"})
    int updateClient(@Param("clientId") UUID clientId, @Param("req") UpdateClientRequestDTO req);
}
