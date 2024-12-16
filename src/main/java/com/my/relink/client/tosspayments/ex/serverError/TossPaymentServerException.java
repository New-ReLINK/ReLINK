package com.my.relink.client.tosspayments.ex.serverError;


import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;

public class TossPaymentServerException extends TossPaymentException {
    public TossPaymentServerException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
