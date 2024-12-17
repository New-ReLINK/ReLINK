package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.QExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomExchangeItemRepositoryImpl implements CustomExchangeItemRepository {

    private final JPAQueryFactory queryFactory;
    QExchangeItem exchangeItem = QExchangeItem.exchangeItem;

    @Override
    public Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, String deposit, Pageable pageable) {
        BooleanBuilder builder = buildSearchCriteria(search, tradeStatus, category);
        long totalCount = queryFactory.selectFrom(exchangeItem)
                .where(builder)
                .fetchCount();
        List<ExchangeItem> items = queryFactory.selectFrom(exchangeItem)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSortOrder(deposit))
                .fetch();
        return new org.springframework.data.domain.PageImpl<>(items, pageable, totalCount);
    }

    // 검색 조건
    private BooleanBuilder buildSearchCriteria(String search, TradeStatus tradeStatus, Category category) {
        BooleanBuilder builder = new BooleanBuilder();
        if (search != null && !search.isEmpty()) {
            builder.and(exchangeItem.name.containsIgnoreCase(search)
                    .or(exchangeItem.desiredItem.containsIgnoreCase(search)));
        }
        if (tradeStatus != null) {
            builder.and(exchangeItem.tradeStatus.eq(tradeStatus));
        }
        if (category != null) {
            builder.and(exchangeItem.category.eq(category));
        }
        return builder;
    }

    // 정렬
    private OrderSpecifier<?> getSortOrder(String deposit) {
        if (deposit == null || deposit.isEmpty()) {
            return exchangeItem.createdAt.desc();
        }
        return switch (deposit.toLowerCase()) {
            case "asc" -> exchangeItem.deposit.asc();
            case "desc" -> exchangeItem.deposit.desc();
            default -> throw new BusinessException(ErrorCode.INVALID_SORT_PARAMETER);
        };
    }
}

