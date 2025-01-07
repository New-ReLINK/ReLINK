package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class TradeInquiryDetailRespDto {

    private ExchangeItemInfoDto exchangeItemInfoDto;
    private TradeStatusInfoDto tradeStatusInfoDto;
    private TradePartnerInfoDto tradePartnerInfoDto;

    public TradeInquiryDetailRespDto(Trade trade, User partner, int trustScoreOfPartner, String requestedItemImageUrl) {
        this.exchangeItemInfoDto = new ExchangeItemInfoDto(trade, requestedItemImageUrl);
        this.tradeStatusInfoDto = new TradeStatusInfoDto(trade);
        this.tradePartnerInfoDto = new TradePartnerInfoDto(partner, trustScoreOfPartner);
    }
}
