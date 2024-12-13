package com.my.relink.controller.trade.dto.request;

import com.my.relink.domain.trade.TradeCancelReason;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TradeCancelReqDto {

    private TradeCancelReason tradeCancelReason;
    private String tradeCancelDescription;

}
