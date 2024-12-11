package com.my.relink.domain.trade;

import lombok.Getter;

@Getter
public enum TradeCancelReason {

    NO_RESPONSE("상대방과 연락이 되지 않아요"),
    CHANGE_OF_MIND("더 이상 교환을 원하지 않아요"),
    PROMISE_VIOLATION("상대방이 약속을 지키지 않아요"),
    OTHER("기타 사유")
    ;


    private final String message;

    TradeCancelReason(String message) {
        this.message = message;
    }
}
