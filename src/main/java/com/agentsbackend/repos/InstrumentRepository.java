package com.agentsbackend.repos;

import com.agentsbackend.entities.Instrument;
import com.agentsbackend.enums.AssetClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mapper
public interface InstrumentRepository {

    @Insert("INSERT INTO instruments (instrument_id, ticker, name, asset_class, industry) " +
            "VALUES (#{instrumentId}, #{ticker}, #{name}, CAST(#{assetClass} AS asset_class), #{industry})")
    void createInstrument(Instrument instrument);

    @Select("SELECT * FROM instruments WHERE ticker = #{ticker}")
    Optional<Instrument> findByTicker(@Param("ticker") String ticker);

    @Select("SELECT * FROM instruments WHERE instrument_id = #{instrumentId,jdbcType=VARCHAR}")
    Optional<Instrument> findById(@Param("instrumentId") UUID instrumentId);

    @Select("SELECT * FROM instruments")
    List<Instrument> findAll();

    @Select("SELECT * FROM instruments WHERE asset_class = CAST(#{assetClass} AS asset_class)")
    List<Instrument> findByAssetClass(@Param("assetClass") AssetClass assetClass);

    @Select("SELECT * FROM instruments WHERE industry = #{industry}")
    List<Instrument> findByIndustry(@Param("industry") String industry);

    @Select("SELECT * FROM instruments WHERE LOWER(ticker) LIKE LOWER(CONCAT('%', #{query}, '%')) " +
            "OR LOWER(name) LIKE LOWER(CONCAT('%', #{query}, '%')) " +
            "ORDER BY CASE WHEN LOWER(ticker) = LOWER(#{query}) THEN 0 ELSE 1 END, ticker ASC")
    List<Instrument> searchByNameOrTicker(@Param("query") String query);

    @Select("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM instruments WHERE ticker = #{ticker}")
    boolean existsByTicker(@Param("ticker") String ticker);
}
