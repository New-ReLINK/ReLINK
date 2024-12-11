package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.trade.Trade;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class TradeStatusInfoDto {

    private String tradeStatus;
    private boolean hasOwnerRequested;
    private boolean hasRequesterRequested;

    public TradeStatusInfoDto(Trade trade) {
        this.tradeStatus = trade.getTradeStatus().getMessage();
        this.hasOwnerRequested = trade.getHasOwnerRequested();
        this.hasRequesterRequested = trade.getHasRequesterRequested();
    }
}
