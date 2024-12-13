package com.my.relink.controller.report.dto.response;

import com.my.relink.domain.item.exchange.ExchangeItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ExchangeItemInfoRespDto {

    private String ownerNickname;
    private String exchangeItemImageUrl;
    private String exchangeItemName;
    private Long exchangeItemId;

    public ExchangeItemInfoRespDto(ExchangeItem exchangeItem, String exchangeItemImageUrl) {
        this.ownerNickname = exchangeItem.getUser().getNickname();
        this.exchangeItemImageUrl = exchangeItemImageUrl;
        this.exchangeItemName = exchangeItem.getName();
        this.exchangeItemId = exchangeItem.getId();
    }
}
