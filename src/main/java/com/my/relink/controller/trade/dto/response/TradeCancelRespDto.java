package com.my.relink.controller.trade.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class TradeCancelRespDto {
    private String partnerExchangeItemName;
    private String partnerNickname;
    private String tradeStartedAt;
    private String partnerExchangeItemImageUrl;

}
