package com.my.relink.domain.item.exchange.repository;

import com.my.relink.domain.category.Category;
import com.my.relink.domain.image.EntityType;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.item.exchange.repository.dto.FindExchangeItemListRepositoryDto;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.QUser;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.my.relink.domain.image.QImage.image;
import static com.my.relink.domain.item.exchange.QExchangeItem.exchangeItem;
import static com.my.relink.domain.review.QReview.review;

@Repository
@RequiredArgsConstructor
public class CustomExchangeItemRepositoryImpl implements CustomExchangeItemRepository {

    private final JPAQueryFactory queryFactory;
    QUser subUser = new QUser("subUser");
    private static final String ORDER_CREATED_AT = "createdAt";
    private static final String ORDER_DEPOSIT = "deposit";

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

    @Override
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

    @Override
    public Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV0(
            String keyword,
            Long categoryId,
            TradeStatus tradeStatus,
            Pageable pageable
    ) {
        List<FindExchangeItemListRepositoryDto> contents = queryFactory
                .select(Projections.constructor(
                        FindExchangeItemListRepositoryDto.class,
                        exchangeItem.id,
                        exchangeItem.name,
                        exchangeItem.tradeStatus,
                        exchangeItem.itemQuality,
                        exchangeItem.desiredItem,
                        getFirstImage(),
                        exchangeItem.user.id,
                        exchangeItem.user.nickname,
                        avgStar(),
                        exchangeItem.description,
                        exchangeItem.category.name,
                        exchangeItem.deposit,
                        exchangeItem.createdAt
                ))
                .from(exchangeItem)
                .leftJoin(exchangeItem.user)
                .leftJoin(image).on(image.entityId.eq(exchangeItem.id))
                .where(
                        nameEq(keyword),
                        categoryIdEq(categoryId),
                        tradeStatusEq(tradeStatus)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .fetch();

        JPAQuery<Long> totalCount = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem);
        return PageableExecutionUtils.getPage(contents, pageable, totalCount::fetchOne);
    }

    @Override
    public Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV1(
            String keyword,
            Long categoryId,
            TradeStatus tradeStatus,
            Pageable pageable
    ) {
        List<Long> ids = queryFactory
                .select(exchangeItem.id)
                .from(exchangeItem)
                .where(
                        nameEq(keyword),
                        tradeStatusEq(tradeStatus),
                        categoryIdEq(categoryId)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .fetch();

        if (CollectionUtils.isEmpty(ids)) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        List<FindExchangeItemListRepositoryDto> contents = queryFactory
                .select(Projections.constructor(
                        FindExchangeItemListRepositoryDto.class,
                        exchangeItem.id,
                        exchangeItem.name,
                        exchangeItem.tradeStatus,
                        exchangeItem.itemQuality,
                        exchangeItem.desiredItem,
                        getFirstImage(),
                        exchangeItem.user.id,
                        exchangeItem.user.nickname,
                        avgStar(),
                        exchangeItem.description,
                        exchangeItem.category.name,
                        exchangeItem.deposit,
                        exchangeItem.createdAt
                ))
                .from(exchangeItem)
                .leftJoin(exchangeItem.user)
                .leftJoin(image).on(image.entityId.eq(exchangeItem.id))
                .where(
                        exchangeItem.id.in(ids)
                )
                .orderBy(getOrderSpecifier(pageable.getSort()))
                .fetch();

        JPAQuery<Long> totalCount = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem);
        return PageableExecutionUtils.getPage(contents, pageable, totalCount::fetchOne);
    }

    @Override
    public Page<FindExchangeItemListRepositoryDto> findAllExchangeItemV2(
            String keyword,
            Long categoryId,
            TradeStatus tradeStatus,
            Long itemId,
            Sort orders
    ) {
        int page = itemId != null ? (int) ((itemId - 20) / 20) : 0;

        List<Long> ids = queryFactory
                .select(exchangeItem.id)
                .from(exchangeItem)
                .where(
                        nameEq(keyword),
                        tradeStatusEq(tradeStatus),
                        categoryIdEq(categoryId),
                        cursorId(itemId)
                )
                .limit(20)
                .orderBy(getOrderSpecifier(orders))
                .fetch();

        if (CollectionUtils.isEmpty(ids)) {
            return new PageImpl<>(new ArrayList<>(), PageRequest.of(0, 20, orders), 0);
        }

        List<FindExchangeItemListRepositoryDto> contents = queryFactory
                .select(Projections.constructor(
                        FindExchangeItemListRepositoryDto.class,
                        exchangeItem.id,
                        exchangeItem.name,
                        exchangeItem.tradeStatus,
                        exchangeItem.itemQuality,
                        exchangeItem.desiredItem,
                        getFirstImage(),
                        exchangeItem.user.id,
                        exchangeItem.user.nickname,
                        avgStar(),
                        exchangeItem.description,
                        exchangeItem.category.name,
                        exchangeItem.deposit,
                        exchangeItem.createdAt
                ))
                .from(exchangeItem)
                .leftJoin(exchangeItem.user)
                .leftJoin(image).on(image.entityId.eq(exchangeItem.id))
                .where(
                        exchangeItem.id.in(ids)
                )
                .orderBy(getOrderSpecifier(orders))
                .fetch();

        JPAQuery<Long> totalCount = queryFactory
                .select(exchangeItem.count())
                .from(exchangeItem);

        return PageableExecutionUtils.getPage(contents, PageRequest.of(page, 20, orders), totalCount::fetchOne);
    }

    private BooleanBuilder cursorId(Long itemId) {
        if (itemId == null) {
            return null;
        }
        return new BooleanBuilder().and(exchangeItem.id.lt(itemId));
    }


    private JPQLQuery<String> getFirstImage() {
        return JPAExpressions.select(image.imageUrl)
                .from(image)
                .where(image.entityId.eq(exchangeItem.id), image.entityType.eq(EntityType.EXCHANGE_ITEM))
                .limit(1);
    }

    private JPQLQuery<Double> avgStar() {
        return JPAExpressions.select(review.star.avg())
                .from(review)
                .where(review.exchangeItem.user.id.eq(exchangeItem.user.id));
    }

    private BooleanExpression tradeStatusEq(TradeStatus tradeStatus) {
        return tradeStatus != null ? exchangeItem.tradeStatus.eq(tradeStatus) : null;
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        return categoryId != null ? exchangeItem.category.id.eq(categoryId) : null;
    }

    private BooleanExpression nameEq(String keyword) {
        return keyword != null ? exchangeItem.name.startsWith(keyword) : null;
    }

    private OrderSpecifier<?>[] getOrderSpecifier(Sort orders) {
        if (orders.isEmpty()) {
            List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, exchangeItem.createdAt));
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, exchangeItem.id));
            return orderSpecifiers.toArray(new OrderSpecifier[0]);
        }

        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        orders.forEach(order -> {
            Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;

            switch (order.getProperty()) {
                case ORDER_CREATED_AT:
                    orderSpecifiers.add(new OrderSpecifier<>(direction, exchangeItem.createdAt));
                    break;
                case ORDER_DEPOSIT:
                    orderSpecifiers.add(new OrderSpecifier<>(direction, exchangeItem.deposit));
                    break;
                default:
                    break;
            }
        });

        orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, exchangeItem.id));
        return orderSpecifiers.toArray(new OrderSpecifier[0]);
    }
}

