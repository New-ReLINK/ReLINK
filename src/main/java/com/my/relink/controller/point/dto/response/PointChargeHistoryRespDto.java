package com.my.relink.controller.point.dto.response;

import com.my.relink.domain.payment.Payment;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Getter
@ToString
public class PointChargeHistoryRespDto {

    private String chargedDateTime;
    private String method;
    private String provider; //method가 간편 결제일 때 선택한 간편 곃제사
    private Integer amount; //충전 금액
    private Integer chargePoint; //충전된 포인트
    private String status; //결제 처리 상태

    public PointChargeHistoryRespDto(Payment payment, String chargedDateTime) {
        this.chargedDateTime = chargedDateTime;
        this.method = payment.getMethod();
        this.provider = payment.getProvider() != null ? payment.getProvider() : null;
        this.amount = payment.getAmount();
        this.chargePoint = payment.getAmount();
        this.status = payment.getStatus();
    }
}
