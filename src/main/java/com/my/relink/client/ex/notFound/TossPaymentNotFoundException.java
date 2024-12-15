package com.my.relink.client.ex.notFound;


import com.my.relink.client.ex.TossPaymentErrorCode;
import com.my.relink.client.ex.TossPaymentException;

public class TossPaymentNotFoundException extends TossPaymentException {

    public TossPaymentNotFoundException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
