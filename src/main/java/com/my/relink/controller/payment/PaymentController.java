package com.my.relink.controller.payment;

import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.controller.payment.dto.response.PaymentRespDto;
import com.my.relink.service.payment.PaymentProcessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentProcessService paymentServiceFacade;

    @PostMapping("/users/point")
    public ResponseEntity<PaymentRespDto> confirmPaymentForPointCharge(@RequestBody @Valid PaymentReqDto paymentReqDto){
        System.out.println("paymentReqDto.getUserId() = " + paymentReqDto.getUserId());
        return ResponseEntity.ok(paymentServiceFacade.processPayment(paymentReqDto));
    }
}