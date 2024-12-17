package com.my.relink.client.tosspayments.ex.badRequest;


import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;

public class TossPaymentBadRequestException extends TossPaymentException {

    public TossPaymentBadRequestException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
