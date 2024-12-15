package com.my.relink.client.ex.serverError;


import com.my.relink.client.ex.TossPaymentErrorCode;
import com.my.relink.client.ex.TossPaymentException;

public class TossPaymentServerException extends TossPaymentException {
    public TossPaymentServerException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
