package com.my.relink.domain.item.exchange.repository;

import com.my.relink.controller.exchangeItem.dto.resp.GetAllExchangeItemsRespDto;
import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.dto.FindExchangeItemListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CustomExchangeItemRepository {
    Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, String deposit, Pageable pageable);

    Page<ExchangeItem> findAvailableItemsByUserId(Long userId, Pageable pageable);

    Page<ExchangeItem> findByUserId(@Param("userId") Long userId, Pageable pageable);

    @Transactional(readOnly = true)
    Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV0(String keyword, Long categoryId, TradeStatus tradeStatus, Pageable pageable);

    @Transactional(readOnly = true)
    Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV1(String keyword, Long categoryId, TradeStatus tradeStatus, Pageable pageable);

    @Transactional(readOnly = true)
    Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV2(String keyword, Long categoryId, TradeStatus tradeStatus, Long itemId, Sort orders);
}
