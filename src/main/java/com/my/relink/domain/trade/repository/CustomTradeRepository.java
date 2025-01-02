package com.my.relink.domain.trade.repository;

import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.trade.repository.dto.TradeWithOwnerItemNameDto;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CustomTradeRepository {
    List<Trade> findByExchangeItemIds(@Param("itemIds") List<Long> itemIds);

    Optional<Trade> findTradeWithDetails(@Param("tradeId") Long tradeId);
    boolean existsByRequesterIdAndTradeStatus(Long userId, TradeStatus tradeStatus);
    Optional<Trade> findByIdWithExchangeItem(@Param("tradeId") Long tradeId);

    Optional<TradeWithOwnerItemNameDto> findTradeWithOwnerItemNameById(Long tradeId);
}
