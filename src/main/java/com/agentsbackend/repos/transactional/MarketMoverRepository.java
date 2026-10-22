package com.agentsbackend.repos.transactional;

import com.agentsbackend.entities.MarketMover;
import org.apache.ibatis.annotations.Select;
import java.util.List;

public interface MarketMoverRepository {
    @Select("SELECT movers.rank, movers.instrument_id, movers.snapshot_id, instruments.ticker, " +
            "movers.price, movers.change_percent, movers.source_market, movers.source_as_of, movers.refreshed_at " +
            "FROM market_movers movers JOIN instruments ON instruments.instrument_id = movers.instrument_id " +
            "ORDER BY movers.rank ASC LIMIT 5")
    List<MarketMover> findLatest();
}