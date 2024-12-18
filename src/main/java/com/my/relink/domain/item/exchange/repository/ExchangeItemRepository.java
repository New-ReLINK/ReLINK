package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    long countByTradeStatusAndUserId(TradeStatus status, Long userId);

    @Query("select ei from ExchangeItem ei join fetch ei.user where ei.id = :itemId and ei.isDeleted = false")
    Optional<ExchangeItem> findByIdWithUser(@Param("itemId") Long itemId);

    Page<ExchangeItem> findByUserId(Long id, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("update ExchangeItem e set e.tradeStatus = com.my.relink.domain.trade.TradeStatus.UNAVAILABLE where e.user.id = :userId")
    void updateTradeStatusToUnavailable(@Param("userId") Long userId);

}
