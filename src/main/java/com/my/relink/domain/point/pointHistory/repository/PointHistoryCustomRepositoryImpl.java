package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.QPointHistory;
import com.my.relink.domain.trade.QTrade;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

import static com.my.relink.domain.point.pointHistory.QPointHistory.*;
import static com.my.relink.domain.trade.QTrade.*;

@Repository
@RequiredArgsConstructor
public class PointHistoryCustomRepositoryImpl implements PointHistoryCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final DateTimeUtil dateTimeUtil;
    @Override
    public PageResponse<PointUsageHistoryRespDto> findPointUsageHistories(User user, int page, int size) {
        QTrade t = trade;
        QPointHistory ph = pointHistory;

        List<PointUsageHistoryRespDto> contents = queryFactory
                .select(Projections.constructor(PointUsageHistoryRespDto.class,
                        t.id,
                        new CaseBuilder()
                                .when(t.requester.eq(user))
                                .then(t.ownerExchangeItem.name)
                                .otherwise(t.requesterExchangeItem.name),
                        t.tradeStatus,
                        new CaseBuilder()
                                .when(ph.pointTransactionType.eq(PointTransactionType.DEPOSIT))
                                .then(ph.amount.multiply(-1))
                                .otherwise((Integer) null),
                        new CaseBuilder()
                                .when(ph.pointTransactionType.eq(PointTransactionType.RETURN))
                                .then(ph.amount)
                                .otherwise((Integer) null),
                        new CaseBuilder()
                                .when(ph.pointTransactionType.eq(PointTransactionType.DEPOSIT))
                                .then(ph.createdAt)
                                .otherwise((LocalDateTime) null),
                        new CaseBuilder()
                                .when(ph.pointTransactionType.eq(PointTransactionType.RETURN))
                                .then(ph.createdAt)
                                .otherwise((LocalDateTime) null)
                ))
                .from(ph)
                .join(ph.trade, t)
                .where(
                        ph.point.user.eq(user)
                                .and(ph.pointTransactionType.in(
                                        PointTransactionType.DEPOSIT,
                                        PointTransactionType.RETURN
                                ))
                ).groupBy(
                        t.id,
                        t.tradeStatus,
                        t.requesterExchangeItem.name,
                        t.ownerExchangeItem.name,
                        t.requester.id
                )
                .orderBy(ph.createdAt.desc())
                .offset((long) page * size)
                .limit(size)
                .fetch();


        contents.forEach(content -> content.formatDateTime(dateTimeUtil));

        Long totalCount = queryFactory
                .select(t.countDistinct())
                .from(ph)
                .join(ph.trade, t)
                .where(
                        ph.point.user.eq(user)
                                .and(ph.pointTransactionType.in(
                                        PointTransactionType.DEPOSIT,
                                        PointTransactionType.RETURN
                                ))
                )
                .fetchOne();

        long count = totalCount != null ? totalCount : 0L;
        int totalPages = (int) Math.ceil((double) count / size);

        return new PageResponse<>(
                contents,
                new PageInfo(
                        totalPages,
                        count,
                        page > 0,
                        page + 1 < totalPages)
        );
    }
}
