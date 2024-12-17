package com.my.relink.service.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PaymentValidation {

    private String fieldName;
    private Object actual;
    private Object expected;

}
