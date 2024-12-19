package com.my.relink.domain.payment.repository.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PointChargeHistoryDto {

    private LocalDateTime chargedDateTime;
    private String method;
    private String provider; //method가 간편 결제일 때 선택한 간편 곃제사
    private Integer amount; //충전 금액
    private Integer chargePoint; //충전된 포인트
    private String status; //결제 처리 상태


    public PointChargeHistoryDto(LocalDateTime chargedDateTime, String method, String provider, Integer amount, Integer chargePoint, String status) {
        this.chargedDateTime = chargedDateTime;
        this.method = method;
        this.provider = provider;
        this.amount = amount;
        this.chargePoint = chargePoint;
        this.status = status;
    }
}
