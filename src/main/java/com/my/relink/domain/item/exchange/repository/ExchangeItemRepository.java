package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExchangeItemRepository extends JpaRepository<ExchangeItem, Long> {

    long countByTradeStatusAndUserId(TradeStatus status, Long userId);
    Page<ExchangeItem> findByUserId(Long id, Pageable pageable);
}
