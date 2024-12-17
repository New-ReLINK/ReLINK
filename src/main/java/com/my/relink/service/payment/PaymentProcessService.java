package com.my.relink.service.payment;

import com.my.relink.client.tosspayments.TossPaymentClient;
import com.my.relink.client.tosspayments.dto.request.TossPaymentReqDto;
import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.client.tosspayments.ex.TossPaymentException;
import com.my.relink.client.tosspayments.feature.PaymentFeature;
import com.my.relink.controller.payment.dto.request.PaymentReqDto;
import com.my.relink.controller.payment.dto.response.PaymentRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentCancelReason;
import com.my.relink.domain.user.User;
import com.my.relink.ex.BusinessException;
import com.my.relink.ex.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class PaymentProcessService {

    private final PaymentService paymentService;

    public PaymentRespDto processPayment(PaymentReqDto paymentReqDto){
        User user = paymentService.validateUserAndPayment(paymentReqDto);
        try{
            TossPaymentRespDto tossPaymentRespDto = paymentService.confirmPayment(paymentReqDto);
            Payment payment = paymentService.savePaymentInfo(paymentReqDto, tossPaymentRespDto, user);
            paymentService.chargePointWithHistory(user, payment, paymentReqDto);
            return new PaymentRespDto(user);
        } catch (TossPaymentException | BusinessException e){
            throw e;
        } catch (Exception e){
            log.error("[결제 프로세스 실패] 예기치 못한 예외 발생. cause = {}, userId = {}", e.getMessage(), user.getId(), e);
            throw new BusinessException(ErrorCode.CRITICAL_PAYMENT_PROCESS_ERROR);
        }
    }


}
