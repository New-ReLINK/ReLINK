package com.my.relink.domain.point.pointHistory.repository;

import com.my.relink.controller.point.dto.response.PointUsageHistoryRespDto;
import com.my.relink.domain.point.pointHistory.PointTransactionType;
import com.my.relink.domain.point.pointHistory.QPointHistory;
import com.my.relink.domain.trade.QTrade;
import com.my.relink.domain.trade.TradeStatus;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

import static com.my.relink.domain.point.pointHistory.QPointHistory.*;
import static com.my.relink.domain.trade.QTrade.*;
import static com.querydsl.jpa.JPAExpressions.*;

@Repository
@RequiredArgsConstructor
public class PointHistoryCustomRepositoryImpl implements PointHistoryCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final DateTimeUtil dateTimeUtil;

    @Override
    public PageResponse<PointUsageHistoryRespDto> findPointUsageHistories(User user, int page, int size) {
        List<Long> tradeIdList = getTradeIdList(user, page, size);
        List<Tuple> pointHistoryTuple = getPointHistoryTuple(tradeIdList, user);
        List<PointUsageHistoryRespDto> contents = convertToDto(pointHistoryTuple, tradeIdList);
        PageInfo pageInfo = getPointUsagePageInfo(user, page, size);

        return new PageResponse<>(contents, pageInfo);
    }


    private PageInfo getPointUsagePageInfo(User user, int page, int size){
        long totalCount = queryFactory
                .select(pointHistory.trade.countDistinct())
                .from(pointHistory)
                .where(
                        pointHistory.point.user.eq(user)
                                .and(pointHistory.pointTransactionType.in(
                                        PointTransactionType.DEPOSIT,
                                        PointTransactionType.RETURN
                                ))
                )
                .fetchOne();

        int totalPages = (int) Math.ceil((double) totalCount / size);

        return new PageInfo (
                totalPages,
                totalCount,
                page > 0,
                page + 1 < totalPages);
    }


    private List<PointUsageHistoryRespDto> convertToDto(List<Tuple> pointHistoryTuple, List<Long> tradeIdList){
        Map<Long, PointUsageHistoryRespDto> dtoMap = new LinkedHashMap<>();
        for (Tuple tuple : pointHistoryTuple) {
            Long tradeId = tuple.get(trade.id);
            TradeStatus tradeStatus = tuple.get(trade.tradeStatus);
            String itemName = tuple.get(Expressions.stringPath("itemName"));
            PointTransactionType type = tuple.get(pointHistory.pointTransactionType);
            Integer amount = tuple.get(pointHistory.amount);
            LocalDateTime createdAt = tuple.get(pointHistory.createdAt);

            PointUsageHistoryRespDto dto = dtoMap.computeIfAbsent(tradeId, k ->
                    PointUsageHistoryRespDto.builder()
                            .tradeId(tradeId)
                            .tradeStatus(tradeStatus.getMessage())
                            .partnerExchangeItemName(itemName)
                            .build());

            if(type == PointTransactionType.DEPOSIT){
                dto.setDepositAmount(amount * -1);
                dto.setDepositDateTime(dateTimeUtil.getUsagePointHistoryFormattedTime(createdAt));
            } else {
                dto.setRefundAmount(amount);
                dto.setRefundDateTime(dateTimeUtil.getUsagePointHistoryFormattedTime(createdAt));
            }
        }

        return tradeIdList.stream()
                .map(dtoMap::get)
                .toList();
    }


    private List<Long> getTradeIdList(User user, int page, int size){
        QTrade t = trade;
        QPointHistory ph = pointHistory;

        List<Tuple> tradeInfoList = queryFactory
                .select(ph.trade.id, ph.createdAt.max())
                .from(ph)
                .where(
                        ph.point.user.eq(user)
                                .and(ph.pointTransactionType.in(
                                        PointTransactionType.RETURN,
                                        PointTransactionType.DEPOSIT
                                ))
                )
                .groupBy(ph.trade.id)
                .orderBy(ph.createdAt.max().desc())
                .offset(page * size)
                .limit(size)
                .fetch();

        return tradeInfoList.stream()
                .map(tradeInfo -> tradeInfo.get(0, Long.class))
                .toList();
    }


    private List<Tuple> getPointHistoryTuple(List<Long> tradeIdList, User user){
        return queryFactory
                .select(
                        trade.id,
                        trade.tradeStatus,
                        new CaseBuilder()
                                .when(trade.requester.eq(user))
                                .then(trade.ownerExchangeItem.name)
                                .otherwise(trade.requesterExchangeItem.name).as("itemName"),
                        pointHistory.pointTransactionType,
                        pointHistory.amount,
                        pointHistory.createdAt
                )
                .from(pointHistory)
                .join(pointHistory.trade, trade)
                .where(trade.id.in(tradeIdList))
                .fetch();
    }
}
