package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomExchangeItemRepository {
    Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, String deposit, Pageable pageable);
}
