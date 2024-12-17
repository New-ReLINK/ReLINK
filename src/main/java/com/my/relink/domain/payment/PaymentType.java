package com.my.relink.domain.payment;

import lombok.Getter;

@Getter
public enum PaymentType {

    POINT_CHARGE("포인트 충전"),

    ;

    private final String message;

    PaymentType(String message) {
        this.message = message;
    }
}
