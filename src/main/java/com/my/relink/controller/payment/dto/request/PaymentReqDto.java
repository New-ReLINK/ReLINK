package com.my.relink.controller.payment.dto.request;

import com.my.relink.client.tosspayments.dto.response.TossPaymentRespDto;
import com.my.relink.domain.payment.Payment;
import com.my.relink.domain.payment.PaymentType;
import com.my.relink.domain.user.User;
import lombok.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor
@Getter
@ToString
@AllArgsConstructor
@Builder
public class PaymentReqDto {


    private String orderId;
    private Integer amount;
    private String paymentKey;
    private Long userId;

    public Payment toEntity(TossPaymentRespDto result, User user, PaymentType paymentType){
        return Payment.builder()
                .method(result.getMethod())
                .user(user)
                .paymentType(paymentType)
                .amount(result.getTotalAmount())
                .status(result.getStatus())
                .paidAt(LocalDateTime.parse(result.getApprovedAt(), DateTimeFormatter.ISO_DATE_TIME))
                .failReason(result.getFailure() != null? result.getFailure().getMessage() : null)
                .merchantUid(result.getOrderId())
                .provider(result.getEasyPay() != null ? result.getEasyPay().getProvider() : null)
                .build();
    }


}
