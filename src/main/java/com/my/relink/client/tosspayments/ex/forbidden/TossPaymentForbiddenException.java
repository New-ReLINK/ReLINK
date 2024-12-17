package com.my.relink.client.tosspayments.ex.forbidden;


import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;

public class TossPaymentForbiddenException extends TossPaymentException {
    public TossPaymentForbiddenException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
