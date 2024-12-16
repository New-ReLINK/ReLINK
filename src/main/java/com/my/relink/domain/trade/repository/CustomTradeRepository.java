package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CustomTradeRepository {
    List<Trade> findByExchangeItemIds(@Param("itemIds") List<Long> itemIds);
}
