package com.my.relink.client.tosspayments;

public class TossPaymentApiPath {
    static final String PAYMENT_CONFIRM = "/v1/payments/confirm";
    static final String PAYMENT_CANCEL = "/v1/payments/{paymentKey}/cancel";
    static final String PAYMENT_INFO = "/v1/payments/{paymentKey}";
}
