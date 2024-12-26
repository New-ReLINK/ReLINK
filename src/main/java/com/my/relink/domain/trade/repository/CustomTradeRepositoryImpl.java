package com.my.relink.domain.trade.repository;

import com.my.relink.domain.item.exchange.QExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.my.relink.domain.trade.QTrade.trade;

@Repository
@RequiredArgsConstructor
public class CustomTradeRepositoryImpl implements CustomTradeRepository {

    private final JPAQueryFactory queryFactory;
    private static final QExchangeItem ownerExchangeItem = QExchangeItem.exchangeItem;
    private static final QUser owner = QUser.user;
    private static final  QExchangeItem requesterExchangeItem = QExchangeItem.exchangeItem;
    private static final QUser requester = QUser.user;

    @Override
    public List<Trade> findByExchangeItemIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        return queryFactory.selectDistinct(trade)
                .from(trade)
                .join(trade.ownerExchangeItem, ownerExchangeItem).fetchJoin()
                .join(ownerExchangeItem.user, owner).fetchJoin()
                .join(trade.requesterExchangeItem, requesterExchangeItem).fetchJoin()
                .join(requesterExchangeItem.user, requester).fetchJoin()
                .where(ownerExchangeItem.id.in(itemIds)
                        .or(requesterExchangeItem.id.in(itemIds)))
                .fetch();
    }

    @Override
    public Optional<Trade> findTradeWithDetails(Long tradeId) {
        QExchangeItem requesterExchangeItemAlias = new QExchangeItem("requesterExchangeItem");
        QExchangeItem ownerExchangeItemAlias = new QExchangeItem("ownerExchangeItem");

        Trade result = queryFactory.selectFrom(trade)
                .leftJoin(trade.requester, requester).fetchJoin()
                .leftJoin(trade.requesterExchangeItem, requesterExchangeItemAlias).fetchJoin()
                .leftJoin(trade.ownerExchangeItem, ownerExchangeItemAlias).fetchJoin()
                .where(trade.id.eq(tradeId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public boolean existsByRequesterIdAndTradeStatus(Long userId, TradeStatus tradeStatus) {
        long count = queryFactory.selectFrom(trade)
                .where(trade.requester.id.eq(userId)
                        .and(trade.tradeStatus.eq(tradeStatus)))
                .fetchCount();

        return count > 0;
    }

    @Override
    public Optional<Trade> findByIdWithExchangeItem(@Param("tradeId") Long tradeId) {
        QExchangeItem requesterExchangeItemAlias = new QExchangeItem("requesterExchangeItem");
        QExchangeItem ownerExchangeItemAlias = new QExchangeItem("ownerExchangeItem");

        Trade result = queryFactory.selectFrom(trade)
                .join(trade.ownerExchangeItem, ownerExchangeItemAlias).fetchJoin()
                .join(trade.requesterExchangeItem, requesterExchangeItemAlias).fetchJoin()
                .where(trade.id.eq(tradeId))
                .fetchOne();

        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Trade> findByExchangeItemId(Long itemId) {
        Trade contents =  queryFactory
                .selectFrom(trade)
                .join(trade.ownerExchangeItem, ownerExchangeItem).fetchJoin()
                .join(ownerExchangeItem.user, owner).fetchJoin()
                .join(trade.requesterExchangeItem, requesterExchangeItem).fetchJoin()
                .join(requesterExchangeItem.user, requester).fetchJoin()
                .where(ownerExchangeItem.id.in(itemId)
                        .or(requesterExchangeItem.id.in(itemId)))
                .fetchOne();
        return Optional.ofNullable(contents);
    }
}
