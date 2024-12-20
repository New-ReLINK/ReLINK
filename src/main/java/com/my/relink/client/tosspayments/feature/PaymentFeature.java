package com.my.relink.client.tosspayments.feature;

import lombok.Getter;

@Getter
public enum PaymentFeature {

    PAYMENT_CONFIRM("결제 승인"),
    PAYMENT_INQUIRY("결제 조회"),
    PAYMENT_CANCEL("결제 취소");

    private final String description;

    PaymentFeature(String description) {
        this.description = description;
    }
}
