package com.my.relink.controller.trade.dto.response;

import com.my.relink.domain.image.Image;
import com.my.relink.domain.item.exchange.ExchangeItem;
import com.my.relink.domain.trade.Trade;
import com.my.relink.domain.user.User;
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

    public static TradeCancelRespDto from(User partner, ExchangeItem partnerExchangeItem, Image partnerExchangeItemImage, String tradeStartedAt){
        return new TradeCancelRespDto(
                partnerExchangeItem.getName(),
                partner.getNickname(),
                tradeStartedAt,
                partnerExchangeItemImage != null ? partnerExchangeItemImage.getImageUrl() : null
        );
    }
}
