package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.TradeStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.my.relink.domain.item.exchange.QExchangeItem.exchangeItem;

@Repository
@RequiredArgsConstructor
public class CustomExchangeItemRepositoryImpl implements CustomExchangeItemRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<ExchangeItem> findAllByCriteria(String search, TradeStatus tradeStatus, Category category, String deposit, Pageable pageable) {
        BooleanBuilder builder = buildSearchCriteria(search, tradeStatus, category);
        List<ExchangeItem> items = queryFactory.selectFrom(exchangeItem)
                .leftJoin(exchangeItem.user).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getSortOrder(deposit))
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem)
                .where(builder);
        long count = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        return new PageImpl<>(items, pageable, count);
    }

    @Override
    public Page<ExchangeItem> findAvailableItemsByUserId(Long userId, Pageable pageable) {
        List<ExchangeItem> items = queryFactory
                .selectFrom(exchangeItem)
                .join(exchangeItem.user).fetchJoin()
                .where(
                        exchangeItem.user.id.eq(userId),
                        exchangeItem.tradeStatus.eq(TradeStatus.AVAILABLE),
                        exchangeItem.isDeleted.isFalse()
                )
                .orderBy(exchangeItem.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem)
                .where(
                        exchangeItem.user.id.eq(userId),
                        exchangeItem.tradeStatus.eq(TradeStatus.AVAILABLE),
                        exchangeItem.isDeleted.isFalse());
        long count = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        return new PageImpl<>(items, pageable, count);
    }

    public Page<ExchangeItem> findByUserId(@Param("userId") Long userId, Pageable pageable) {
        List<ExchangeItem> items = queryFactory
                .selectFrom(exchangeItem)
                .join(exchangeItem.user).fetchJoin()
                .where(exchangeItem.user.id.eq(userId))
                .orderBy(exchangeItem.modifiedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        JPAQuery<Long> countQuery = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem)
                .where(exchangeItem.user.id.eq(userId));
        long count = Optional.ofNullable(countQuery.fetchOne()).orElse(0L);
        return new PageImpl<>(items, pageable, count);
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
        return deposit.equalsIgnoreCase("asc")
                ? exchangeItem.deposit.asc()
                : exchangeItem.deposit.desc();
    }

}

