package com.my.relink.domain.trade;

import lombok.Getter;

@Getter
public enum TradeStatus {
    AVAILABLE("교환 가능"),
    IN_EXCHANGE("교환 중"),
    EXCHANGED("교환 완료"),
    CANCELED("교환 취소")

    ;


    private final String message;

    TradeStatus(String message) {
        this.message = message;
    }
}
