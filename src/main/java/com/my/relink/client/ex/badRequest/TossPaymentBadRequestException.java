package com.my.relink.client.ex.badRequest;


import com.my.relink.client.ex.TossPaymentErrorCode;
import com.my.relink.client.ex.TossPaymentException;

public class TossPaymentBadRequestException extends TossPaymentException {

    public TossPaymentBadRequestException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
