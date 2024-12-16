package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.QExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomExchangeItemRepositoryImpl implements CustomExchangeItemRepository {

    private final JPAQueryFactory queryFactory;
    QExchangeItem exchangeItem = QExchangeItem.exchangeItem;

    @Override
    public Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        // 검색어 조건
        if (search != null && !search.isEmpty()) {
            builder.and(exchangeItem.name.containsIgnoreCase(search)
                    .or(exchangeItem.desiredItem.containsIgnoreCase(search)));
        }
        // 교환 상태 조건
        if (tradeStatus != null) {
            builder.and(exchangeItem.tradeStatus.eq(tradeStatus));
        }

        // 카테고리 조건
        if (category != null) {
            builder.and(exchangeItem.category.eq(category));
        }
        // 데이터 조회
        long totalCount = queryFactory.selectFrom(exchangeItem)
                .where(builder)
                .fetchCount();

        List<ExchangeItem> items = queryFactory.selectFrom(exchangeItem)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSortOrder(pageable.getSort()))
                .fetch();
        return new org.springframework.data.domain.PageImpl<>(items, pageable, totalCount);
    }

    private OrderSpecifier<?> getSortOrder(Sort sort) {
        if (sort.isSorted()) {
            for (Sort.Order order : sort) {
                if (order.getProperty().equalsIgnoreCase("deposit")) {
                    return order.getDirection() == Sort.Direction.ASC
                            ? exchangeItem.deposit.asc()
                            : exchangeItem.deposit.desc();
                }
            }
        }
        return exchangeItem.id.desc();
    }
}
