package com.my.relink.domain.trade.repository;

import com.my.relink.domain.item.exchange.QExchangeItem;
import com.my.relink.domain.trade.QTrade;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CustomTradeRepositoryImpl implements CustomTradeRepository {

    private final JPAQueryFactory queryFactory;

    public List<Trade> findByExchangeItemIds(List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return List.of();
        }
        QTrade trade = QTrade.trade;
        QExchangeItem ownerExchangeItem = QExchangeItem.exchangeItem;
        QUser ownerUser = QUser.user;
        QExchangeItem requesterExchangeItem = QExchangeItem.exchangeItem;
        QUser requesterUser = QUser.user;

        return queryFactory.selectDistinct(trade)
                .from(trade)
                .join(trade.ownerExchangeItem, ownerExchangeItem).fetchJoin()
                .join(ownerExchangeItem.user, ownerUser).fetchJoin()
                .join(trade.requesterExchangeItem, requesterExchangeItem).fetchJoin()
                .join(requesterExchangeItem.user, requesterUser).fetchJoin()
                .where(ownerExchangeItem.id.in(itemIds)
                        .or(requesterExchangeItem.id.in(itemIds)))
                .fetch();
    }
}
