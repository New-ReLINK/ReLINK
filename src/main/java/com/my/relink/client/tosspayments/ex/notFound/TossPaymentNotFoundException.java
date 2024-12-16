package com.my.relink.client.tosspayments.ex.notFound;


import com.my.relink.client.tosspayments.ex.TossPaymentErrorCode;
import com.my.relink.client.tosspayments.ex.TossPaymentException;

public class TossPaymentNotFoundException extends TossPaymentException {

    public TossPaymentNotFoundException(TossPaymentErrorCode errorCode, String paymentKey) {
        super(errorCode, paymentKey, errorCode.getMessage());
    }
}
