package com.my.relink.controller.report.dto.response;

import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import com.my.relink.util.DateTimeUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TradeInfoRespDto {

    private String partnerNickname;
    private String partnerExchangeItemName;
    private Long partnerExchangeItemId;
    private String partnerExchangeItemImageUrl;
    private String exchangeStartDate;

    public TradeInfoRespDto(Trade trade, ExchangeItem item, String partnerExchangeItemImageUrl, DateTimeUtil dateTimeUtil) {
        this.partnerNickname = item.getUser().getNickname();
        this.partnerExchangeItemName = item.getName();
        this.partnerExchangeItemId = item.getId();
        this.partnerExchangeItemImageUrl = partnerExchangeItemImageUrl;
        this.exchangeStartDate = dateTimeUtil.getExchangeStartFormattedTime(trade.getCreatedAt());
    }
}
