package com.my.relink.controller.point.dto.response;

import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.repository.dto.PointChargeHistoryDto;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class PointChargeHistoryRespDto {

    private String chargedDateTime;
    private String method;
    private String provider; //method가 간편 결제일 때 선택한 간편 곃제사
    private Integer amount; //충전 금액
    private Integer chargePoint; //충전된 포인트
    private String status; //결제 처리 상태

    public PointChargeHistoryRespDto(String chargedDateTime, PointChargeHistoryDto dto) {
        this.chargedDateTime = chargedDateTime;
        this.method = dto.getMethod();
        this.provider = dto.getProvider() != null ? dto.getProvider() : null;
        this.amount = dto.getAmount();
        this.chargePoint = dto.getAmount();
        this.status = dto.getStatus();
    }
}
