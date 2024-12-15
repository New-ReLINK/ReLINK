package com.my.relink.client.ex.unAuthorized;

import com.my.relink.client.ex.TossPaymentErrorCode;
import com.my.relink.client.ex.TossPaymentException;

public class TossPaymentUnauthorizedException extends TossPaymentException {
    public TossPaymentUnauthorizedException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
