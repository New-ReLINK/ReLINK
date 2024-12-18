package com.my.relink.domain.payment.repository;

import com.my.relink.controller.point.dto.response.PointChargeHistoryRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentType;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import com.my.relink.util.page.PageInfo;
import com.my.relink.util.page.PageResponse;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import static com.my.relink.domain.payment.QPayment.*;

@Repository
@RequiredArgsConstructor
public class PaymentCustomRepositoryImpl implements PaymentCustomRepository{

    private final JPAQueryFactory queryFactory;
    private final DateTimeUtil dateTimeUtil;

    @Override
    public PageResponse<PointChargeHistoryRespDto> findPointChargeHistories(User user, int page, int size) {
        List<Payment> paymentList = queryFactory
                .selectFrom(payment)
                .where(
                        payment.user.eq(user)
                                .and(payment.paymentType.eq(PaymentType.POINT_CHARGE))
                )
                .orderBy(payment.createdAt.desc())
                .offset(page * size)
                .limit(size)
                .fetch();

        List<PointChargeHistoryRespDto> contents = convertToDto(paymentList);
        PageInfo pageInfo = getPointChargePageInfo(user, page, size);

        return new PageResponse<>(contents, pageInfo);
    }

    private List<PointChargeHistoryRespDto> convertToDto(List<Payment> paymentList){
        return paymentList.stream()
                .map(payment -> new PointChargeHistoryRespDto(
                        payment,
                        dateTimeUtil.getUsagePointHistoryFormattedTime(payment.getPaidAt())
                ))
                .toList();
    }

    private PageInfo getPointChargePageInfo(User user, int page, int size){
        Long totalCount = queryFactory
                .select(payment.countDistinct())
                .from(payment)
                .where(
                        payment.user.eq(user)
                        .and(payment.paymentType.eq(PaymentType.POINT_CHARGE))
                )
                .fetchOne();

        long count = totalCount != null ? totalCount : 0L;
        int totalPages = (int) Math.ceil((double) count / size);

        return new PageInfo (
                totalPages,
                count,
                page > 0,
                page + 1 < totalPages);
    }
}
