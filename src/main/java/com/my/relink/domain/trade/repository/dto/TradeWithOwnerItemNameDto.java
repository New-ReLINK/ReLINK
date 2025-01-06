package com.my.relink.domain.trade.repository.dto;

import com.my.relink.domain.trade.Trade;
import lombok.Getter;

@Getter
public class TradeWithOwnerItemNameDto {
    private final Trade trade;
    private final String itemName;

    public TradeWithOwnerItemNameDto(Trade trade, String itemName) {
        this.trade = trade;
        this.itemName = itemName;
    }
}