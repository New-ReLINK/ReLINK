package com.my.relink.controller.trade.dto.request;

import com.my.relink.domain.trade.TradeCancelReason;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeCancelReqDto {

    @NotNull(message = "교환 취소 사유를 입력하세요.")
    private TradeCancelReason tradeCancelReason;
    private String tradeCancelDescription;

}
