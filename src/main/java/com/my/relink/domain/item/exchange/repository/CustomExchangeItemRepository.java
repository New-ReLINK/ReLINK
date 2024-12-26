package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

public interface CustomExchangeItemRepository {
    Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, String deposit, Pageable pageable);

    Page<ExchangeItem> findAvailableItemsByUserId(Long userId, Pageable pageable);

    Page<ExchangeItem> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
