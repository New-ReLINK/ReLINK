package com.my.relink.controller.payment.dto.response;

import com.my.relink.domain.point.pointHistory.PointHistory;
import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PaymentRespDto {
    private Long userId;
    private String transactionType;

    public PaymentRespDto(PointHistory pointHistory) {
        this.userId = pointHistory.getPoint().getUser().getId();
        this.transactionType = pointHistory.getPointTransactionType().toString();
    }
}
