package com.my.relink.client.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class TossPaymentCancelReqDto {

    private String cancelReason; //취소 사유
}
