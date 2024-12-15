package com.my.relink.client.dto.response;

import com.my.relink.client.ex.TossPaymentErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class TossPaymentErrorRespDto {
    private String code;
    private String message;
    private String paymentKey;

    public TossPaymentErrorCode getErrorCode() {
        return TossPaymentErrorCode.fromString(code);
    }
}

