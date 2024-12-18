package com.my.relink.client.tosspayments.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TossPaymentReqDto {

    public static final String FIELD_ORDER_ID = "orderId";
    public static final String FIELD_AMOUNT = "amount";
    public static final String FIELD_PAYMENT_KEY = "paymentKey";

    private String orderId;
    private Integer amount;
    private String paymentKey;

    public TossPaymentReqDto(String orderId, Integer amount, String paymentKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentKey = paymentKey;
    }
}
