package com.my.relink.service.payment.ex;

import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;

public class PaymentCancelFailException extends BusinessException {
    public PaymentCancelFailException(ErrorCode errorCode) {
        super(errorCode);
    }
}
