package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long>, CustomExchangeItemRepository {

    long countByTradeStatusAndUserId(TradeStatus status, Long userId);

    Page<ExchangeItem> findByUserId(Long id, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update ExchangeItem e set e.tradeStatus = com.my.relink.domain.trade.TradeStatus.UNAVAILABLE where e.user.id = :userId")
    void updateTradeStatusToUnavailable(@Param("userId") Long userId);

    @Query("SELECT e FROM ExchangeItem e WHERE e.user.id = :userId AND e.tradeStatus = com.my.relink.domain.trade.TradeStatus.AVAILABLE ORDER BY e.modifiedAt DESC")
    Page<ExchangeItem> findAvailableItemsByUserIdOrderByModifiedAt(@Param("userId") Long userId, Pageable pageable);
}
