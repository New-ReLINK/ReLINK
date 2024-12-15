package com.my.relink.client.ex.forbidden;


import com.my.relink.client.ex.TossPaymentErrorCode;
import com.my.relink.client.ex.TossPaymentException;

public class TossPaymentForbiddenException extends TossPaymentException {
    public TossPaymentForbiddenException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
