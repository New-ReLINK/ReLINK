package com.my.relink.domain.trade;

import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.Getter;

import java.util.Arrays;

@Getter
public enum TradeStatus {
    AVAILABLE("교환 가능"),
    IN_EXCHANGE("교환 중"),
    EXCHANGED("교환 완료"),
    CANCELED("교환 취소"),
    IN_DELIVERY("배송 중")

    ;


    private final String message;

    TradeStatus(String message) {
        this.message = message;
    }

    public static TradeStatus statusOf(String message){
        return Arrays.stream(values())
                .filter(tradeStatus -> tradeStatus.getMessage().equals(message))
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.TRADE_STATUS_NOT_FOUND));
    }
}
