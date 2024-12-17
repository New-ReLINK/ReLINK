package com.my.relink.service.payment;

import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.controller.payment.dto.response.PaymentRespDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class PaymentProcessService {

    private final PaymentService paymentService;

    //TODO 구현 예정
    public PaymentRespDto processPayment(PaymentReqDto paymentReqDto){
        return null;
    }

}
