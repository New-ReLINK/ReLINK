package com.my.relink.client.tosspayments.ex.unAuthorized;

import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;

public class TossPaymentUnauthorizedException extends TossPaymentException {
    public TossPaymentUnauthorizedException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
