package com.agentsbackend.repos;

import com.agentsbackend.entities.Watchlist;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface WatchlistRepository {

    @Insert("INSERT INTO watchlists (watchlist_id, client_id, instrument_id) " +
            "VALUES (#{watchlistId}, #{clientId}, #{instrumentId}) " +
            "ON CONFLICT (client_id, instrument_id) DO NOTHING")
    int add(Watchlist watchlist);

    @Select("SELECT w.watchlist_id, w.client_id, w.instrument_id, i.ticker, i.name AS instrument_name, w.added_at " +
            "FROM watchlists w JOIN instruments i ON i.instrument_id = w.instrument_id " +
            "WHERE w.client_id = #{clientId} ORDER BY w.added_at DESC")
    @Results(id = "watchlistResult", value = {
            @Result(property = "watchlistId", column = "watchlist_id"),
            @Result(property = "clientId", column = "client_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "ticker", column = "ticker"),
            @Result(property = "instrumentName", column = "instrument_name"),
            @Result(property = "addedAt", column = "added_at")
    })
    List<Watchlist> findByClientId(@Param("clientId") UUID clientId);

    @Select("SELECT w.watchlist_id, w.client_id, w.instrument_id, i.ticker, i.name AS instrument_name, w.added_at " +
            "FROM watchlists w JOIN instruments i ON i.instrument_id = w.instrument_id " +
            "WHERE w.client_id = #{clientId} AND w.instrument_id = #{instrumentId}")
    @Results(id = "singleWatchlistResult", value = {
            @Result(property = "watchlistId", column = "watchlist_id"),
            @Result(property = "clientId", column = "client_id"),
            @Result(property = "instrumentId", column = "instrument_id"),
            @Result(property = "ticker", column = "ticker"),
            @Result(property = "instrumentName", column = "instrument_name"),
            @Result(property = "addedAt", column = "added_at")
    })
    Optional<Watchlist> findByClientAndInstrument(@Param("clientId") UUID clientId,
                                                   @Param("instrumentId") UUID instrumentId);

    @Delete("DELETE FROM watchlists WHERE client_id = #{clientId} AND instrument_id = #{instrumentId}")
    int delete(@Param("clientId") UUID clientId, @Param("instrumentId") UUID instrumentId);
}
