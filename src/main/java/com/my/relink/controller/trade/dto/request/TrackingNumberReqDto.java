package com.my.relink.controller.trade.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.validation.annotation.Validated;

@Getter
@AllArgsConstructor
public class TrackingNumberReqDto {
    @NotBlank(message = "운송장 번호는 필수 입력 값입니다.")
    private String trackingNumber;
}
