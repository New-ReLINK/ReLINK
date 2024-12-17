package com.my.relink.controller.payment;

import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.controller.payment.dto.response.PaymentRespDto;
import com.my.relink.service.payment.PaymentServiceFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentServiceFacade paymentServiceFacade;

    @PostMapping("/users/point")
    public ResponseEntity<PaymentRespDto> confirmPaymentForPointCharge(@RequestBody PaymentReqDto paymentReqDto){
        return ResponseEntity.ok(paymentServiceFacade.processPayment(paymentReqDto));
    }
}