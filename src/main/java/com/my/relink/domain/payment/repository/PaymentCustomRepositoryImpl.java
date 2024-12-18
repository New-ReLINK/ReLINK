package com.my.relink.domain.payment.repository;

import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentType;
import com.my.relink.domain.payment.repository.dto.PointChargeHistoryDto;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.my.relink.domain.payment.QPayment.*;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public List<PointChargeHistoryDto> findPointChargeHistories(User user, int page, int size) {
        return queryFactory
                .select(Projections.constructor(PointChargeHistoryDto.class,
                        payment.paidAt,
                        payment.method,
                        payment.provider,
                        payment.amount,
                        payment.amount,
                        payment.status
                ))
                .from(payment)
                .where(pointChargeCondition(user))
                .orderBy(payment.createdAt.desc())
                .offset(page * size)
                .limit(size)
                .fetch();
    }

    public PageInfo getPointChargePageInfo(User user, int page, int size){
        long totalCount = queryFactory
                .select(payment.countDistinct())
                .from(payment)
                .where(pointChargeCondition(user))
                .fetchOne();

        int totalPages = (int) Math.ceil((double) totalCount / size);

        return new PageInfo (
                totalPages,
                totalCount,
                page > 0,
                page + 1 < totalPages);
    }

    private BooleanExpression pointChargeCondition(User user) {
        return payment.user.eq(user).and(payment.paymentType.eq(PaymentType.POINT_CHARGE));
    }
}
